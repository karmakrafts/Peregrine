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
import io.karma.peregrine.dispose.DispositionHandler;
import io.karma.peregrine.reload.ReloadHandler;
import io.karma.peregrine.shader.ShaderLoader;
import io.karma.peregrine.shader.ShaderProgram;
import io.karma.peregrine.shader.ShaderProgramBuilder;
import io.karma.peregrine.texture.TextureFactories;
import io.karma.peregrine.uniform.UniformTypeFactories;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class Peregrine {
    public static final String MODID = "peregrine";
    public static final Logger LOGGER = LogManager.getLogger("Peregrine");
    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private static ExecutorService executorService;
    private static ReloadHandler reloadHandler;
    private static DispositionHandler dispositionHandler;
    private static TextureFactories textureFactories;
    private static UniformTypeFactories uniformTypeFactories;
    private static Function<Consumer<UniformBufferBuilder>, UniformBuffer> uniformBufferFactory;
    private static Function<Consumer<ShaderProgramBuilder>, ShaderProgram> shaderProgramFactory;
    private static Supplier<ShaderLoader> defaultShaderLoader;
    private static Supplier<UniformBuffer> globalUniforms;

    private static boolean supportsBindlessTextures;
    private static boolean supportsBinaryShaderCaching;
    private static boolean supportsStorageBuffers;
    private static boolean supportsTessellationShaders;
    private static boolean supportsComputeShaders;
    private static boolean supportsLongShaderType;
    private static boolean supportsDoubleShaderType;
    private static boolean supportsDirectStateAccess;

    // @formatter:off
    private Peregrine() {}
    // @formatter:on

    public static boolean isLoaded() {
        return FMLLoader.getLoadingModList().getModFileById(MODID) != null;
    }

    private static void ensureInitialized() {
        if (!isInitialized.get()) {
            throw new IllegalStateException("Peregrine is not initialized");
        }
    }

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

    private static void queryExtensions() {
        LOGGER.info("Querying OpenGL extensions");
        supportsBindlessTextures = queryExtension("GL_ARB_bindless_texture");
        supportsBinaryShaderCaching = queryExtension("GL_ARB_get_program_binary");
        supportsStorageBuffers = queryExtension("GL_ARB_shader_storage_buffer_object");
        supportsTessellationShaders = queryExtension("GL_ARB_tessellation_shader");
        supportsComputeShaders = queryExtension("GL_ARB_compute_shader");
        supportsLongShaderType = queryExtension("GL_ARB_gpu_shader_int64");
        supportsDoubleShaderType = queryExtension("GL_ARB_gpu_shader_fp64");
        supportsDirectStateAccess = queryExtension("GL_ARB_direct_state_access");
    }

    static void init(final ExecutorService executorService,
                     final ReloadHandler reloadHandler,
                     final DispositionHandler dispositionHandler,
                     final UniformTypeFactories uniformTypeFactories,
                     final TextureFactories textureFactories,
                     final Function<Consumer<UniformBufferBuilder>, UniformBuffer> uniformBufferFactory,
                     final Function<Consumer<ShaderProgramBuilder>, ShaderProgram> shaderProgramFactory,
                     final Supplier<ShaderLoader> defaultShaderLoader,
                     final Supplier<UniformBuffer> globalUniforms) {
        if (!isInitialized.compareAndSet(false, true)) {
            throw new IllegalStateException("Peregrine is already initialized");
        }

        LOGGER.info("Initializing Peregrine");
        Peregrine.executorService = executorService;
        Peregrine.reloadHandler = reloadHandler;
        Peregrine.dispositionHandler = dispositionHandler;
        Peregrine.textureFactories = textureFactories;
        Peregrine.uniformTypeFactories = uniformTypeFactories;
        Peregrine.uniformBufferFactory = uniformBufferFactory;
        Peregrine.shaderProgramFactory = shaderProgramFactory;
        Peregrine.defaultShaderLoader = defaultShaderLoader;
        Peregrine.globalUniforms = globalUniforms;

        queryExtensions();
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

    public static TextureFactories getTextureFactories() {
        ensureInitialized();
        return textureFactories;
    }

    public static UniformTypeFactories getUniformTypeFactories() {
        ensureInitialized();
        return uniformTypeFactories;
    }

    public static Function<Consumer<UniformBufferBuilder>, UniformBuffer> getUniformBufferFactory() {
        ensureInitialized();
        return uniformBufferFactory;
    }

    public static Function<Consumer<ShaderProgramBuilder>, ShaderProgram> getShaderProgramFactory() {
        ensureInitialized();
        return shaderProgramFactory;
    }

    public static ShaderLoader getDefaultShaderLoader() {
        ensureInitialized();
        return defaultShaderLoader.get();
    }

    public static UniformBuffer getGlobalUniforms() {
        ensureInitialized();
        return globalUniforms.get();
    }

    public static boolean supportsBindlessTextures() {
        ensureInitialized();
        return supportsBindlessTextures;
    }

    public static boolean supportsBinaryShaderCaching() {
        ensureInitialized();
        return supportsBinaryShaderCaching;
    }

    public static boolean supportsStorageBuffers() {
        ensureInitialized();
        return supportsStorageBuffers;
    }

    public static boolean supportsTessellationShaders() {
        ensureInitialized();
        return supportsTessellationShaders;
    }

    public static boolean supportsComputeShaders() {
        ensureInitialized();
        return supportsComputeShaders;
    }

    public static boolean supportsLongShaderType() {
        ensureInitialized();
        return supportsLongShaderType;
    }

    public static boolean supportsDoubleShaderType() {
        ensureInitialized();
        return supportsDoubleShaderType;
    }

    public static boolean supportsDirectStateAccess() {
        ensureInitialized();
        return supportsDirectStateAccess;
    }
}
