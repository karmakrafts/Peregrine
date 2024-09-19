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

package io.karma.peregrine.api;

import io.karma.peregrine.api.buffer.UniformBuffer;
import io.karma.peregrine.api.buffer.UniformBufferFactory;
import io.karma.peregrine.api.buffer.UniformBufferProvider;
import io.karma.peregrine.api.dispose.DispositionHandler;
import io.karma.peregrine.api.font.FontFamily;
import io.karma.peregrine.api.font.FontFamilyFactory;
import io.karma.peregrine.api.font.FontRendererFactory;
import io.karma.peregrine.api.framebuffer.Framebuffer;
import io.karma.peregrine.api.framebuffer.FramebufferFactory;
import io.karma.peregrine.api.reload.ReloadHandler;
import io.karma.peregrine.api.shader.*;
import io.karma.peregrine.api.state.BlendModeFactory;
import io.karma.peregrine.api.state.RenderTypeBuilder;
import io.karma.peregrine.api.state.RenderTypeFactory;
import io.karma.peregrine.api.target.RenderTarget;
import io.karma.peregrine.api.target.RenderTargetFactories;
import io.karma.peregrine.api.texture.Texture;
import io.karma.peregrine.api.texture.TextureFactories;
import io.karma.peregrine.api.texture.TextureFilter;
import io.karma.peregrine.api.texture.TextureWrapMode;
import io.karma.peregrine.api.uniform.MatrixType;
import io.karma.peregrine.api.uniform.ScalarType;
import io.karma.peregrine.api.uniform.UniformTypeFactories;
import io.karma.peregrine.api.uniform.VectorType;
import io.karma.peregrine.api.util.DI;
import io.karma.peregrine.api.util.Requires;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * The main Peregrine API interface. Can be used for querying
 * capabilities of the underlying implementation and retrieving
 * references to internal factory functions.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
public final class Peregrine {
    public static final String MODID = "peregrine";
    public static final Logger LOGGER = LogManager.getLogger("Peregrine");
    public static final ResourceLocation FONT_FAMILY_REGISTRY_NAME = new ResourceLocation(MODID, "font_families");

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
    private static FramebufferFactory framebufferFactory;
    @OnlyIn(Dist.CLIENT)
    private static ShaderLoaderProvider defaultShaderLoader;
    @OnlyIn(Dist.CLIENT)
    private static UniformBufferProvider globalUniforms;
    @OnlyIn(Dist.CLIENT)
    private static ShaderPreProcessorProvider defaultShaderPreProcessor;
    @OnlyIn(Dist.CLIENT)
    private static RenderTargetFactories renderTargetFactories;
    @OnlyIn(Dist.CLIENT)
    private static RenderTypeFactory renderTypeFactory;
    @OnlyIn(Dist.CLIENT)
    private static BlendModeFactory blendModeFactory;
    @OnlyIn(Dist.CLIENT)
    private static FontRendererFactory fontRendererFactory;

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
    private static ShaderBinaryFormat shaderBinaryFormat;
    @OnlyIn(Dist.CLIENT)
    private static int maxTextureSize;

    @OnlyIn(Dist.CLIENT)
    private static boolean isSodiumInstalled;
    @OnlyIn(Dist.CLIENT)
    private static boolean isIrisInstalled;

    private static boolean isDevelopmentEnvironment;
    private static FontFamilyFactory fontFamilyFactory;

    // @formatter:off
    private Peregrine() {}
    // @formatter:on

    // ========= Loader specific functionality =========

    /**
     * Creates a new deferred register for {@link FontFamily}s.
     *
     * @param modId the mod ID to create the new deferred register for.
     * @return a new deferred register associated with the given mod ID.
     */
    public static DeferredRegister<FontFamily> createFontFamilyRegister(final String modId) {
        return DeferredRegister.create(Objects.requireNonNull(getRegistry(FONT_FAMILY_REGISTRY_NAME)), modId);
    }

    /**
     * Retrieves the registry with the given identifier.
     *
     * @param name the identifier of the registry to retrieve.
     * @param <T>  the internal element type of the registry.
     * @return the registry with the give name if it exists, null otherwise.
     */
    @SuppressWarnings("all")
    private static <T> @Nullable IForgeRegistry<T> getRegistry(final ResourceLocation name) {
        final var registry = RegistryManager.ACTIVE.<T>getRegistry(name);
        if (registry != null) {
            return registry;
        }
        return RegistryManager.FROZEN.getRegistry(name);
    }

    // ================================================

    private static void ensureInitialized() {
        Requires.that(isInitialized.get(), "Peregrine is not initialized");
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
        supportsBinaryShaderCaching = queryExtension("GL_ARB_get_program_binary") && shaderBinaryFormat.isValid();
        supportsStorageBuffers = queryExtension("GL_ARB_shader_storage_buffer_object");
        supportsTessellationShaders = queryExtension("GL_ARB_tessellation_shader");
        supportsComputeShaders = queryExtension("GL_ARB_compute_shader");
        supportsLongShaderType = queryExtension("GL_ARB_gpu_shader_int64");
        supportsDoubleShaderType = queryExtension("GL_ARB_gpu_shader_fp64");
        supportsDirectStateAccess = queryExtension("GL_ARB_direct_state_access");
    }

    @Internal
    public static void init(final ExecutorService executorService,
                            final ReloadHandler reloadHandler,
                            final DispositionHandler dispositionHandler,
                            final DI di) {
        Requires.that(isInitialized.compareAndSet(false, true), "Peregrine is already initialized");

        LOGGER.info("Initializing Peregrine");
        final var environment = Objects.requireNonNull(di.get(Environment.class)).props;
        isDevelopmentEnvironment = (Boolean) environment.get("is_dev_environment");

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Peregrine.executorService = executorService;
            Peregrine.reloadHandler = reloadHandler;
            Peregrine.dispositionHandler = dispositionHandler;
            textureFactories = di.getSafe(TextureFactories.class);
            uniformTypeFactories = di.getSafe(UniformTypeFactories.class);
            uniformBufferFactory = di.getSafe(UniformBufferFactory.class);
            shaderProgramFactory = di.getSafe(ShaderProgramFactory.class);
            framebufferFactory = di.getSafe(FramebufferFactory.class);
            defaultShaderLoader = di.getSafe(ShaderLoaderProvider.class);
            globalUniforms = di.getSafe(UniformBufferProvider.class);
            defaultShaderPreProcessor = di.getSafe(ShaderPreProcessorProvider.class);
            renderTargetFactories = di.getSafe(RenderTargetFactories.class);
            renderTypeFactory = di.getSafe(RenderTypeFactory.class);
            blendModeFactory = di.getSafe(BlendModeFactory.class);
            fontRendererFactory = di.getSafe(FontRendererFactory.class);
            shaderBinaryFormat = di.getSafe(ShaderBinaryFormat.class);

            if (shaderBinaryFormat.isValid()) {
                LOGGER.info("Using shader binary format 0x{}", Integer.toHexString(shaderBinaryFormat.value()));
            }

            queryExtensions();
            maxTextureSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
            LOGGER.info("Max texture size is {}x{} pixels", maxTextureSize, maxTextureSize);

            isSodiumInstalled = (Boolean) environment.get("is_sodium_installed");
            isIrisInstalled = (Boolean) environment.get("is_iris_installed");
        });

        fontFamilyFactory = di.getSafe(FontFamilyFactory.class);
    }

    /**
     * Retrieves the executor service used by Peregrine internally.
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the executor service used by Peregrine internally.
     */
    public static ExecutorService getExecutorService() {
        ensureInitialized();
        return executorService;
    }

    /**
     * Retrieves the reload handler instance.
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the reload handler instance.
     */
    public static ReloadHandler getReloadHandler() {
        ensureInitialized();
        return reloadHandler;
    }

    /**
     * Retrieves the dispose handler instance.
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the dispose handler instance.
     */
    public static DispositionHandler getDispositionHandler() {
        ensureInitialized();
        return dispositionHandler;
    }

    /**
     * Retrieves the font family factory.
     * This can be used for creating new font family registry entries.
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the font family factory.
     */
    public static FontFamilyFactory getFontFamilyFactory() {
        ensureInitialized();
        return fontFamilyFactory;
    }

    /**
     * Determines whether Peregrine is running in a development environment.
     *
     * @return true if Peregrine is loaded in a development environment.
     */
    public static boolean isDevelopmentEnvironment() {
        ensureInitialized();
        return isDevelopmentEnvironment;
    }

    /**
     * Retrieves the texture factories interface.
     * <p>
     * You usually don't want to call this directly, instead use
     * {@link Texture#get(int)},
     * {@link Texture#get(ResourceLocation)}
     * or {@link Texture#create(TextureFilter, TextureFilter, TextureWrapMode, TextureWrapMode)}.
     * <p>
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the texture factories interface.
     */
    @OnlyIn(Dist.CLIENT)
    public static TextureFactories getTextureFactories() {
        ensureInitialized();
        return textureFactories;
    }

    /**
     * Retrieves the uniform type factories interface.
     * <p>
     * You usually don't want to call this directly, instead use
     * {@link ScalarType#create(String)},
     * {@link VectorType#create(String)}
     * or {@link MatrixType#create(String)}.
     * <p>
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the uniform type factories interface.
     */
    @OnlyIn(Dist.CLIENT)
    public static UniformTypeFactories getUniformTypeFactories() {
        ensureInitialized();
        return uniformTypeFactories;
    }

    /**
     * Retrieves the uniform buffer factory.
     * <p>
     * You usually don't want to call this directly, instead use
     * {@link UniformBuffer#create(Consumer)}.
     * <p>
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the uniform buffer factory.
     */
    @OnlyIn(Dist.CLIENT)
    public static UniformBufferFactory getUniformBufferFactory() {
        ensureInitialized();
        return uniformBufferFactory;
    }

    /**
     * Retrieves the shader program factory.
     * <p>
     * You usually don't want to call this directly, instead use
     * {@link ShaderProgram#create(Consumer)}.
     * <p>
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the shader program factory.
     */
    @OnlyIn(Dist.CLIENT)
    public static ShaderProgramFactory getShaderProgramFactory() {
        ensureInitialized();
        return shaderProgramFactory;
    }

    /**
     * Retrieves the default shader loader used by Peregrine.
     * The implementation of this loader may vary based on the
     * supported platform extensions/features.
     * <p>
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the default shader loader used by Peregrine.
     */
    @OnlyIn(Dist.CLIENT)
    public static ShaderLoader getDefaultShaderLoader() {
        ensureInitialized();
        return defaultShaderLoader.get();
    }

    /**
     * Retrieves the global uniform buffer used by Peregrine.
     * This contains {@code ProjMat}, {@code ModelViewMat},
     * {@code ColorModulator} and {@code Time}.
     * <p>
     * You usually don't want to call this directly, instead use
     * {@link ShaderProgramBuilder#globalUniforms()}.
     * <p>
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the global uniform buffer used by Peregrine.
     */
    @OnlyIn(Dist.CLIENT)
    public static UniformBuffer getGlobalUniforms() {
        ensureInitialized();
        return globalUniforms.get();
    }

    /**
     * Retrieves the default shader pre-processor used by Peregrine.
     * <p>
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the default shader pre-processor used by Peregrine.
     */
    @OnlyIn(Dist.CLIENT)
    public static ShaderPreProcessor getDefaultShaderPreProcessor() {
        ensureInitialized();
        return defaultShaderPreProcessor.get();
    }

    /**
     * Retrieves the framebuffer factory.
     * <p>
     * You usually don't want to call this directly, instead use
     * {@link Framebuffer#create(Consumer)}.
     * <p>
     * The API must be initialized before this function is called,
     * so you should make Peregrine an {@code AFTER} dependency for your mod.
     *
     * @return the framebuffer factory.
     */
    @OnlyIn(Dist.CLIENT)
    public static FramebufferFactory getFramebufferFactory() {
        ensureInitialized();
        return framebufferFactory;
    }

    /**
     * Retrieves the render target factories.
     * <p>
     * You usually don't want to call this directly, instead use
     * {@link RenderTarget#get(int)} or
     * one of the getters provided by this interface like {@link RenderTargetFactories#getMainTarget()}.
     *
     * @return the render target factories.
     */
    @OnlyIn(Dist.CLIENT)
    public static RenderTargetFactories getRenderTargetFactories() {
        ensureInitialized();
        return renderTargetFactories;
    }

    // TODO: document this
    @OnlyIn(Dist.CLIENT)
    public static RenderType createRenderType(final Consumer<RenderTypeBuilder> callback) {
        ensureInitialized();
        return renderTypeFactory.apply(callback);
    }

    // TODO: document this
    @OnlyIn(Dist.CLIENT)
    public static BlendModeFactory getBlendModeFactory() {
        ensureInitialized();
        return blendModeFactory;
    }

    // TODO: document this
    @OnlyIn(Dist.CLIENT)
    public static FontRendererFactory getFontRendererFactory() {
        ensureInitialized();
        return fontRendererFactory;
    }

    /**
     * Determines whether <b>GL_ARB_bindless_texture</b> is supported
     * on the current platform.
     *
     * @return true if <b>GL_ARB_bindless_texture</b> is supported.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean supportsBindlessTextures() {
        ensureInitialized();
        return supportsBindlessTextures;
    }

    /**
     * Determines whether <b>GL_ARB_get_program_binary</b> is supported
     * on the current platform.
     *
     * @return true if <b>GL_ARB_get_program_binary</b> is supported.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean supportsBinaryShaderCaching() {
        ensureInitialized();
        return supportsBinaryShaderCaching;
    }

    /**
     * Determines whether <b>GL_ARB_shader_storage_buffer_object</b> is supported
     * on the current platform.
     *
     * @return true if <b>GL_ARB_shader_storage_buffer_object</b> is supported.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean supportsStorageBuffers() {
        ensureInitialized();
        return supportsStorageBuffers;
    }

    /**
     * Determines whether <b>GL_ARB_tessellation_shader</b> is supported
     * on the current platform.
     *
     * @return true if <b>GL_ARB_tessellation_shader</b> is supported.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean supportsTessellationShaders() {
        ensureInitialized();
        return supportsTessellationShaders;
    }

    /**
     * Determines whether <b>GL_ARB_compute_shader</b> is supported
     * on the current platform.
     *
     * @return true if <b>GL_ARB_compute_shader</b> is supported.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean supportsComputeShaders() {
        ensureInitialized();
        return supportsComputeShaders;
    }

    /**
     * Determines whether <b>GL_ARB_gpu_shader_int64</b> is supported
     * on the current platform.
     *
     * @return true if <b>GL_ARB_gpu_shader_int64</b> is supported.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean supportsLongShaderType() {
        ensureInitialized();
        return supportsLongShaderType;
    }

    /**
     * Determines whether <b>GL_ARB_gpu_shader_fp64</b> is supported
     * on the current platform.
     *
     * @return true if <b>GL_ARB_gpu_shader_fp64</b> is supported.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean supportsDoubleShaderType() {
        ensureInitialized();
        return supportsDoubleShaderType;
    }

    /**
     * Determines whether <b>GL_ARB_direct_state_access</b> is supported
     * on the current platform.
     *
     * @return true if <b>GL_ARB_direct_state_access</b> is supported.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean supportsDirectStateAccess() {
        ensureInitialized();
        return supportsDirectStateAccess;
    }

    /**
     * Retrieves the shader binary format used by Peregrine
     * on the current platform if present.
     *
     * @return the shader binary format used by Peregrine.
     */
    @OnlyIn(Dist.CLIENT)
    public static ShaderBinaryFormat getShaderBinaryFormat() {
        ensureInitialized();
        return shaderBinaryFormat;
    }

    /**
     * Retrieves the maximum texture size supported by
     * the current platform.
     *
     * @return the maximum texture size supported by the current platform.
     */
    @OnlyIn(Dist.CLIENT)
    public static int getMaxTextureSize() {
        ensureInitialized();
        return maxTextureSize;
    }

    /**
     * Determines whether Sodium (Rubidium/Embeddium) is
     * installed in the current environment.
     *
     * @return true if Sodium (Rubidium/Embeddium) is installed
     * in the current environment.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean isSodiumInstalled() {
        ensureInitialized();
        return isSodiumInstalled;
    }

    /**
     * Determines whether Iris (Oculus) is
     * installed in the current environment.
     *
     * @return true if Iris (Oculus) is installed
     * in the current environment.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean isIrisInstalled() {
        ensureInitialized();
        return isIrisInstalled;
    }

    // TODO: document this
    public record Environment(Map<String, Object> props) {
    }
}
