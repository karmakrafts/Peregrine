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

package io.karma.peregrine;

import io.karma.peregrine.buffer.UniformBuffer;
import io.karma.peregrine.buffer.UniformBufferBuilder;
import io.karma.peregrine.buffer.UniformBufferFactory;
import io.karma.peregrine.buffer.UniformBufferProvider;
import io.karma.peregrine.dispose.DispositionHandler;
import io.karma.peregrine.font.FontFamily;
import io.karma.peregrine.reload.ReloadHandler;
import io.karma.peregrine.shader.*;
import io.karma.peregrine.texture.TextureFactories;
import io.karma.peregrine.uniform.UniformTypeFactories;
import io.karma.peregrine.util.DI;
import io.karma.peregrine.util.ShaderBinaryFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
public final class Peregrine {
    public static final String MODID = "peregrine";
    public static final Logger LOGGER = LogManager.getLogger("Peregrine");
    static final ResourceLocation FONT_FAMILY_REGISTRY_NAME = new ResourceLocation(MODID, "font_families");

    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private static ExecutorService executorService;
    private static ReloadHandler reloadHandler;
    private static DispositionHandler dispositionHandler;

    @OnlyIn(Dist.CLIENT)
    private static TextureFactories textureFactories;
    @OnlyIn(Dist.CLIENT)
    private static UniformTypeFactories uniformTypeFactories;
    @OnlyIn(Dist.CLIENT)
    private static UniformBufferFactory uniformBufferFactory;
    @OnlyIn(Dist.CLIENT)
    private static ShaderProgramFactory shaderProgramFactory;
    @OnlyIn(Dist.CLIENT)
    private static Supplier<ShaderLoader> defaultShaderLoader;
    @OnlyIn(Dist.CLIENT)
    private static Supplier<UniformBuffer> globalUniforms;

    @OnlyIn(Dist.CLIENT)
    private static boolean supportsBindlessTextures;
    @OnlyIn(Dist.CLIENT)
    private static boolean supportsBinaryShaderCaching;
    @OnlyIn(Dist.CLIENT)
    private static boolean supportsStorageBuffers;
    @OnlyIn(Dist.CLIENT)
    private static boolean supportsTessellationShaders;
    @OnlyIn(Dist.CLIENT)
    private static boolean supportsComputeShaders;
    @OnlyIn(Dist.CLIENT)
    private static boolean supportsLongShaderType;
    @OnlyIn(Dist.CLIENT)
    private static boolean supportsDoubleShaderType;
    @OnlyIn(Dist.CLIENT)
    private static boolean supportsDirectStateAccess;
    @OnlyIn(Dist.CLIENT)
    private static int shaderBinaryFormat;

    private static IForgeRegistry<FontFamily> fontFamilyRegistry;

    // @formatter:off
    private Peregrine() {}
    // @formatter:on

    public static boolean isLoaded() {
        return FMLLoader.getLoadingModList().getModFileById(MODID) != null;
    }

    public static DeferredRegister<FontFamily> createFontFamilyRegister(final String modId) {
        return DeferredRegister.create(fontFamilyRegistry, modId);
    }

    @SuppressWarnings("all")
    private static <T> IForgeRegistry<T> getRegistry(final ResourceLocation name) {
        final var registry = RegistryManager.ACTIVE.<T>getRegistry(name);
        if (registry != null) {
            return registry;
        }
        return RegistryManager.FROZEN.getRegistry(name);
    }

    private static void ensureInitialized() {
        if (!isInitialized.get()) {
            throw new IllegalStateException("Peregrine is not initialized");
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean queryExtension(final String name) {
        try {
            final var caps = GL.getCapabilities();
            final var value = GLCapabilities.class.getField(name).getBoolean(caps);
            LOGGER.info("Extension {}: {}", name, value);
            return value;
        }
        catch (Throwable error) {
            LOGGER.warn("Could not query extension", error);
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void queryExtensions() {
        LOGGER.info("Querying OpenGL extensions");
        supportsBindlessTextures = queryExtension("GL_ARB_bindless_texture");
        supportsBinaryShaderCaching = queryExtension("GL_ARB_get_program_binary") && shaderBinaryFormat != -1;
        supportsStorageBuffers = queryExtension("GL_ARB_shader_storage_buffer_object");
        supportsTessellationShaders = queryExtension("GL_ARB_tessellation_shader");
        supportsComputeShaders = queryExtension("GL_ARB_compute_shader");
        supportsLongShaderType = queryExtension("GL_ARB_gpu_shader_int64");
        supportsDoubleShaderType = queryExtension("GL_ARB_gpu_shader_fp64");
        supportsDirectStateAccess = queryExtension("GL_ARB_direct_state_access");
    }

    private static void queryRegistries() {
        LOGGER.info("Querying registries");
        fontFamilyRegistry = getRegistry(FONT_FAMILY_REGISTRY_NAME);
    }

    static void init(final ExecutorService executorService,
                     final ReloadHandler reloadHandler,
                     final DispositionHandler dispositionHandler,
                     final DI di) {
        if (!isInitialized.compareAndSet(false, true)) {
            throw new IllegalStateException("Peregrine is already initialized");
        }

        LOGGER.info("Initializing Peregrine");

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (shaderBinaryFormat != -1) {
                LOGGER.info("Using shader binary format 0x{}", Integer.toHexString(shaderBinaryFormat));
            }

            Peregrine.executorService = executorService;
            Peregrine.reloadHandler = reloadHandler;
            Peregrine.dispositionHandler = dispositionHandler;
            Peregrine.textureFactories = di.get(TextureFactories.class);
            Peregrine.uniformTypeFactories = di.get(UniformTypeFactories.class);
            Peregrine.uniformBufferFactory = di.get(UniformBufferFactory.class);
            Peregrine.shaderProgramFactory = di.get(ShaderProgramFactory.class);
            Peregrine.defaultShaderLoader = di.get(ShaderLoaderProvider.class);
            Peregrine.globalUniforms = di.get(UniformBufferProvider.class);
            Peregrine.shaderBinaryFormat = Objects.requireNonNull(di.get(ShaderBinaryFormat.class)).value();

            queryExtensions();
        });

        queryRegistries();
    }

    public static ExecutorService getExecutorService() {
        ensureInitialized();
        return executorService;
    }

    public static ReloadHandler getReloadHandler() {
        ensureInitialized();
        return reloadHandler;
    }

    public static DispositionHandler getDispositionHandler() {
        ensureInitialized();
        return dispositionHandler;
    }

    @OnlyIn(Dist.CLIENT)
    public static TextureFactories getTextureFactories() {
        ensureInitialized();
        return textureFactories;
    }

    @OnlyIn(Dist.CLIENT)
    public static UniformTypeFactories getUniformTypeFactories() {
        ensureInitialized();
        return uniformTypeFactories;
    }

    @OnlyIn(Dist.CLIENT)
    public static Function<Consumer<UniformBufferBuilder>, UniformBuffer> getUniformBufferFactory() {
        ensureInitialized();
        return uniformBufferFactory;
    }

    @OnlyIn(Dist.CLIENT)
    public static Function<Consumer<ShaderProgramBuilder>, ShaderProgram> getShaderProgramFactory() {
        ensureInitialized();
        return shaderProgramFactory;
    }

    @OnlyIn(Dist.CLIENT)
    public static ShaderLoader getDefaultShaderLoader() {
        ensureInitialized();
        return defaultShaderLoader.get();
    }

    @OnlyIn(Dist.CLIENT)
    public static UniformBuffer getGlobalUniforms() {
        ensureInitialized();
        return globalUniforms.get();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean supportsBindlessTextures() {
        ensureInitialized();
        return supportsBindlessTextures;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean supportsBinaryShaderCaching() {
        ensureInitialized();
        return supportsBinaryShaderCaching;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean supportsStorageBuffers() {
        ensureInitialized();
        return supportsStorageBuffers;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean supportsTessellationShaders() {
        ensureInitialized();
        return supportsTessellationShaders;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean supportsComputeShaders() {
        ensureInitialized();
        return supportsComputeShaders;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean supportsLongShaderType() {
        ensureInitialized();
        return supportsLongShaderType;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean supportsDoubleShaderType() {
        ensureInitialized();
        return supportsDoubleShaderType;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean supportsDirectStateAccess() {
        ensureInitialized();
        return supportsDirectStateAccess;
    }

    @OnlyIn(Dist.CLIENT)
    public static int getShaderBinaryFormat() {
        ensureInitialized();
        return shaderBinaryFormat;
    }
}
