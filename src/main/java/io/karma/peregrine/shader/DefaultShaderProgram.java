/*
 * Copyright 2024 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.karma.peregrine.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import io.karma.peregrine.Peregrine;
import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.buffer.UniformBuffer;
import io.karma.peregrine.hooks.PeregrineRenderSystem;
import io.karma.peregrine.hooks.PeregrineShaderAdaptor;
import io.karma.peregrine.reload.Reloadable;
import io.karma.peregrine.uniform.DefaultUniformCache;
import io.karma.peregrine.uniform.Uniform;
import io.karma.peregrine.uniform.UniformCache;
import io.karma.peregrine.util.HashUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.system.MemoryStack;

import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultShaderProgram extends ShaderStateShard implements ShaderProgram, Reloadable {
    private final int id;
    private final VertexFormat vertexFormat;
    private final ArrayList<ShaderObject> objects;
    private final Consumer<ShaderProgram> bindCallback;
    private final Consumer<ShaderProgram> unbindCallback;
    private final Object2IntOpenHashMap<String> uniformLocations = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<String> uniformBlockIndices = new Object2IntOpenHashMap<>();
    private final UniformCache uniformCache;
    private final HashMap<String, UniformBuffer> uniformBuffers;
    private final Object2IntOpenHashMap<String> samplerIds;
    private final Int2ObjectArrayMap<Sampler> samplers;
    private final LinkedHashMap<String, Object> constants;
    private final LinkedHashMap<String, Object> defines;
    private final Supplier<ShaderLoader> cacheSupplier;

    private final ArrayList<Sampler> dynamicSamplers = new ArrayList<>();
    private final PeregrineShaderAdaptor extendedShaderAdaptor = new PeregrineShaderAdaptor(this);

    private boolean isLinked;
    private boolean isRelinkRequested;
    private boolean isBound;

    DefaultShaderProgram(final VertexFormat vertexFormat,
                         final ArrayList<ShaderObject> objects,
                         final LinkedHashMap<String, ? extends Uniform<?>> uniforms,
                         final HashMap<String, UniformBuffer> uniformBuffers,
                         final Consumer<ShaderProgram> bindCallback,
                         final Consumer<ShaderProgram> unbindCallback,
                         final Object2IntOpenHashMap<String> samplerIds,
                         final LinkedHashMap<String, Object> constants,
                         final LinkedHashMap<String, Object> defines,
                         final Int2ObjectArrayMap<IntSupplier> staticSamplers,
                         final Supplier<ShaderLoader> cacheSupplier) {
        this.vertexFormat = vertexFormat;
        this.objects = objects;
        this.bindCallback = bindCallback;
        this.unbindCallback = unbindCallback;
        this.samplerIds = samplerIds;
        samplers = new Int2ObjectArrayMap<>(samplerIds.size()); // Pre-allocate sampler texture buffer
        this.constants = constants;
        this.defines = defines;
        this.cacheSupplier = cacheSupplier;

        id = GL20.glCreateProgram();
        Peregrine.LOGGER.debug("Created new shader program {}", id);
        uniformCache = new DefaultUniformCache(uniforms);
        this.uniformBuffers = uniformBuffers;

        PeregrineMod.DISPOSE_HANDLER.register(this);
        PeregrineMod.RELOAD_HANDLER.register(this);

        // Set up static samplers
        for (final var sampler : staticSamplers.int2ObjectEntrySet()) {
            final var samplerId = sampler.getIntKey();
            final var textureId = sampler.getValue();
            // @formatter:off
            final var name = samplerIds.object2IntEntrySet()
                .stream()
                .filter(e -> e.getIntValue() == samplerId)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow();
            // @formatter:on
            final var samplerInstance = StaticSampler.create(samplerId, name, textureId);
            samplers.put(samplerId, samplerInstance);
            if (samplerInstance.isDynamic()) {
                // If ARB_bindless_texture is not available, these will be created as dynamic samplers
                dynamicSamplers.add(samplerInstance);
            }
        }

        // Set up dynamic samplers
        for (final var sampler : samplerIds.object2IntEntrySet()) {
            final var samplerId = sampler.getIntValue();
            if (staticSamplers.containsKey(samplerId)) {
                continue; // We don't want to process the static samplers
            }
            final var samplerInstance = new DynamicSampler(samplerId, sampler.getKey());
            samplers.put(samplerId, samplerInstance);
            dynamicSamplers.add(samplerInstance);
        }
    }

    private void setupUniformBuffers() {
        // Set up uniform buffers
        for (final var buffer : uniformBuffers.entrySet()) {
            buffer.getValue().setup(buffer.getKey(), this);
        }
    }

    private void setupAttributes() {
        // Bind vertex format attribute locations for VS ins
        final var attribs = vertexFormat.getElementAttributeNames();
        for (var i = 0; i < attribs.size(); i++) {
            final var attrib = attribs.get(i);
            GL20.glBindAttribLocation(id, i, attrib);
            Peregrine.LOGGER.debug("Bound vertex format attribute {}={} for program {}", attrib, i, id);
        }
    }

    @Override
    public boolean isAttached(final ShaderObject object) {
        try (final var stack = MemoryStack.stackPush()) {
            final var attachedShaderCount = GL20.glGetProgrami(id, GL20.GL_ATTACHED_SHADERS);
            final var attachedShaders = stack.mallocInt(attachedShaderCount);
            GL20.glGetAttachedShaders(id, null, attachedShaders);
            for (var i = 0; i < attachedShaderCount; i++) {
                if (attachedShaders.get(i) != object.getId()) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private void checkLinkStatus() {
        if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            Peregrine.LOGGER.error("Could not link shader program {}: {}", id, GL20.glGetProgramInfoLog(id));
        }
    }

    @Override
    public void prepare(final ResourceProvider resourceProvider) {
        unbind();
        isLinked = false;
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
        final var cacheDir = FMLLoader.getGamePath().resolve("pda").resolve("shaders");
        if (!Files.exists(cacheDir)) {
            try {
                Files.createDirectories(cacheDir);
            }
            catch (Throwable error) {
                Peregrine.LOGGER.error("Could not create shader cache directories", error);
            }
        }

        GL20.glUseProgram(0);
        final var cache = cacheSupplier.get();
        cache.prepareProgram(this);

        if (!cache.loadProgram(cacheDir, resourceProvider, this)) {
            setupAttributes();
            var shouldLink = true;
            for (final var object : objects) {
                shouldLink &= object.recompile(cacheDir, this, resourceProvider);
            }
            if (shouldLink) {
                Peregrine.LOGGER.debug("Relinking program {}", id);
                GL20.glLinkProgram(id);
                checkLinkStatus();
                cache.saveProgram(cacheDir, resourceProvider, this);
            }
        }
        else {
            checkLinkStatus();
        }

        uniformLocations.clear();
        uniformCache.updateAll(); // Flag all uniforms to be re-applied statically
        for (final var sampler : samplers.values()) {
            sampler.setup(this);
        }
        uniformBlockIndices.clear();
        setupUniformBuffers();

        isLinked = true;
    }

    @Override
    public ShaderLoader getLoader() {
        return cacheSupplier.get();
    }

    @Override
    public int getUniformLocation(final String name) {
        return uniformLocations.computeIfAbsent(name, (String n) -> GL20.glGetUniformLocation(id, n));
    }

    @Override
    public int getUniformBlockIndex(final String name) {
        return uniformBlockIndices.computeIfAbsent(name, (String n) -> GL31.glGetUniformBlockIndex(id, n));
    }

    @Override
    public Map<String, UniformBuffer> getUniformBuffers() {
        return uniformBuffers;
    }

    @Override
    public Map<String, Object> getDefines() {
        return defines;
    }

    @Override
    public Map<String, Object> getConstants() {
        return constants;
    }

    @Override
    public void setSampler(final String name, final int textureId) {
        final var sampler = getSampler(name);
        if (!(sampler instanceof DynamicSampler dynamicSampler)) {
            throw new IllegalArgumentException(String.format("Sampler '%s' is not dynamic", name));
        }
        dynamicSampler.setTextureId(() -> textureId);
    }

    @Override
    public void setSampler(final String name, final ResourceLocation location) {
        final var manager = Minecraft.getInstance().getTextureManager();
        final var texture = manager.getTexture(location);
        setSampler(name, texture.getId());
    }

    @Override
    public Sampler getSampler(final String name) {
        return samplers.get(samplerIds.getInt(name));
    }

    @Override
    public UniformCache getUniforms() {
        return uniformCache;
    }

    @Override
    public ShaderStateShard asStateShard() {
        return this;
    }

    @Override
    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void unbind() {
        if (!isBound || !isLinked) {
            return;
        }
        unbindCallback.accept(this);
        // Unbind/disable samplers
        for (final var sampler : dynamicSamplers) {
            sampler.unbind(this);
        }
        // Undbind/disable UBOs
        for (final var buffer : uniformBuffers.entrySet()) {
            buffer.getValue().unbind(buffer.getKey(), this);
        }
        GL20.glUseProgram(0);
        isBound = false;
    }

    @Override
    public void bind() {
        if (isBound || !isLinked) {
            return;
        }
        GL20.glUseProgram(id);
        // Bind/enable UBOs
        for (final var buffer : uniformBuffers.entrySet()) {
            buffer.getValue().bind(buffer.getKey(), this);
        }
        // Bind/enable samplers
        for (final var sampler : dynamicSamplers) {
            sampler.bind(this);
        }
        bindCallback.accept(this);
        // Update uniforms
        uniformCache.applyAll(this);
        isBound = true;
    }

    @Override
    public void dispose() {
        isLinked = false;
        // Detach and free all shader objects
        for (final var object : objects) {
            final var objectId = object.getId();
            if (isAttached(object)) {
                GL20.glDetachShader(id, objectId);
            }
            GL20.glDeleteShader(objectId);
        }
        GL20.glDeleteProgram(id);
    }

    @Override
    public boolean isLinked() {
        return isLinked;
    }

    @Override
    public void setupRenderState() {
        PeregrineRenderSystem.getInstance().peregrine$setShader(extendedShaderAdaptor);
    }

    @Override
    public void clearRenderState() {
        PeregrineRenderSystem.getInstance().peregrine$setShader(null);
    }

    @Override
    public void requestRelink() {
        isRelinkRequested = true;
    }

    @Override
    public boolean isRelinkRequested() {
        return isRelinkRequested;
    }

    @Override
    public ShaderObject getObject(final ShaderType type) { // @formatter:off
        return objects.stream()
            .filter(obj -> obj.getType() == type)
            .findFirst()
            .orElseThrow();
    } // @formatter:on

    @Override
    public List<ShaderObject> getObjects() {
        return objects;
    }

    @Override
    public @NotNull String toString() {
        return String.format("DefaultShaderProgram[id=%d,objects=%s]", id, objects);
    }

    @Override
    public int hashCode() {
        return HashUtils.combine(vertexFormat.hashCode(),
            objects.hashCode(),
            uniformCache.hashCode(),
            HashUtils.hash(uniformBuffers),
            samplerIds.hashCode(),
            HashUtils.hashValuesAsStrings(constants),
            HashUtils.hashValuesAsStrings(defines));
    }
}
