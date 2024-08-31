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

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.buffer.UniformBuffer;
import io.karma.peregrine.texture.Texture;
import io.karma.peregrine.uniform.Uniform;
import io.karma.peregrine.uniform.UniformType;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
public class DefaultShaderProgramBuilder implements ShaderProgramBuilder {
    private static final Consumer<ShaderProgram> IDENTITY_CALLBACK = program -> {
    };

    private final ArrayList<ShaderObject> objects = new ArrayList<>();
    private final LinkedHashMap<String, Uniform<?>> uniforms = new LinkedHashMap<>();
    private final HashMap<String, UniformBuffer> uniformBuffers = new HashMap<>();
    private final LinkedHashMap<String, Object> constants = new LinkedHashMap<>(); // Don't care about (un)boxing here
    private final Object2IntOpenHashMap<String> samplers = new Object2IntOpenHashMap<>();
    private final Int2ObjectArrayMap<IntSupplier> staticSamplers = new Int2ObjectArrayMap<>();
    private final LinkedHashMap<String, Object> defines = new LinkedHashMap<>();
    private Supplier<ShaderLoader> shaderLoaderSupplier = PeregrineMod.SHADER_LOADER;
    private VertexFormat format = DefaultVertexFormat.POSITION;
    private Consumer<ShaderProgram> bindCallback = IDENTITY_CALLBACK;
    private Consumer<ShaderProgram> unbindCallback = IDENTITY_CALLBACK;
    private int currentSamplerId;

    // @formatter:off
    private DefaultShaderProgramBuilder() {}
    // @formatter:on

    public static ShaderProgram build(final Consumer<ShaderProgramBuilder> callback) {
        final var builder = new DefaultShaderProgramBuilder();
        callback.accept(builder);
        return builder.build();
    }

    DefaultShaderProgram build() {
        return new DefaultShaderProgram(format,
            objects,
            uniforms,
            uniformBuffers,
            bindCallback,
            unbindCallback,
            samplers,
            constants,
            defines,
            staticSamplers,
            shaderLoaderSupplier);
    }

    @Override
    public ShaderProgramBuilder define(final String name) {
        if (defines.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Define '%s' already exists", name));
        }
        defines.put(name, 1);
        return this;
    }

    @Override
    public ShaderProgramBuilder define(final String name, final boolean value) {
        if (defines.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Define '%s' already exists", name));
        }
        defines.put(name, value ? 1 : 0);
        return this;
    }

    @Override
    public ShaderProgramBuilder define(final String name, final int value) {
        if (defines.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Define '%s' already exists", name));
        }
        defines.put(name, value);
        return this;
    }

    @Override
    public ShaderProgramBuilder define(final String name, final float value) {
        if (defines.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Define '%s' already exists", name));
        }
        defines.put(name, value);
        return this;
    }

    @Override
    public ShaderProgramBuilder constant(final String name, final int value) {
        if (constants.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Constant '%s' is already defined", name));
        }
        constants.put(name, value);
        return this;
    }

    @Override
    public ShaderProgramBuilder constant(final String name, final float value) {
        if (constants.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Constant '%s' is already defined", name));
        }
        constants.put(name, value);
        return this;
    }

    @Override
    public ShaderProgramBuilder constant(final String name, final boolean value) {
        if (constants.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Constant '%s' is already defined", name));
        }
        constants.put(name, value);
        return this;
    }

    @Override
    public ShaderProgramBuilder sampler(final String name) {
        if (samplers.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Sampler '%s' is already defined", name));
        }
        samplers.put(name, currentSamplerId++);
        return this;
    }

    @Override
    public ShaderProgramBuilder sampler(final String name, final IntSupplier textureId) {
        if (samplers.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Sampler '%s' is already defined", name));
        }
        final var id = currentSamplerId++;
        samplers.put(name, id);
        staticSamplers.put(id, textureId);
        return this;
    }

    @Override
    public ShaderProgramBuilder sampler(final String name, final ResourceLocation location) {
        return sampler(name, Texture.create(location)::getId);
    }

    @Override
    public ShaderProgramBuilder format(final VertexFormat format) {
        this.format = format;
        return this;
    }

    @Override
    public ShaderProgramBuilder shader(final Consumer<ShaderObjectBuilder> callback) {
        final var builder = new DefaultShaderObjectBuilder();
        callback.accept(builder);
        objects.add(builder.build());
        return this;
    }

    @Override
    public ShaderProgramBuilder uniform(final String name, final UniformType type) {
        if (uniforms.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Uniform '%s' is already defined", name));
        }
        if (!type.isSupported()) {
            throw new IllegalArgumentException("Unsupported uniform type");
        }
        uniforms.put(name, type.create(name));
        return this;
    }

    @Override
    public ShaderProgramBuilder uniforms(final String name, final UniformBuffer buffer) {
        if (uniformBuffers.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Uniform block '%s' is already defined", name));
        }
        uniformBuffers.put(name, buffer);
        return this;
    }

    @Override
    public ShaderProgramBuilder onBind(final Consumer<ShaderProgram> bindCallback) {
        if (this.bindCallback == IDENTITY_CALLBACK) {
            this.bindCallback = bindCallback;
            return this;
        }
        this.bindCallback = this.bindCallback.andThen(bindCallback);
        return this;
    }

    @Override
    public ShaderProgramBuilder onUnbind(final Consumer<ShaderProgram> unbindCallback) {
        if (this.unbindCallback == IDENTITY_CALLBACK) {
            this.unbindCallback = unbindCallback;
            return this;
        }
        this.unbindCallback = this.unbindCallback.andThen(unbindCallback);
        return this;
    }

    @Override
    public ShaderProgramBuilder loader(final Supplier<ShaderLoader> loaderSupplier) {
        shaderLoaderSupplier = loaderSupplier;
        return this;
    }

    @Override
    public ShaderProgramBuilder globalUniforms() {
        uniforms("Globals", PeregrineMod.getGlobalUniforms());
        return this;
    }
}
