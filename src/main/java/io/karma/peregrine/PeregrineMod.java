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

import com.mojang.blaze3d.systems.RenderSystem;
import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.api.Peregrine.Environment;
import io.karma.peregrine.api.buffer.UniformBuffer;
import io.karma.peregrine.api.buffer.UniformBufferFactory;
import io.karma.peregrine.api.buffer.UniformBufferProvider;
import io.karma.peregrine.api.font.FontFamilyFactory;
import io.karma.peregrine.api.font.FontRendererFactory;
import io.karma.peregrine.api.framebuffer.FramebufferFactory;
import io.karma.peregrine.api.shader.*;
import io.karma.peregrine.api.state.BlendModeFactory;
import io.karma.peregrine.api.state.RenderTypeFactory;
import io.karma.peregrine.api.target.RenderTargetFactories;
import io.karma.peregrine.api.texture.TextureFactories;
import io.karma.peregrine.api.uniform.MatrixType;
import io.karma.peregrine.api.uniform.ScalarType;
import io.karma.peregrine.api.uniform.UniformTypeFactories;
import io.karma.peregrine.api.uniform.VectorType;
import io.karma.peregrine.api.util.DI;
import io.karma.peregrine.buffer.DefaultUniformBufferBuilder;
import io.karma.peregrine.dispose.DefaultDispositionHandler;
import io.karma.peregrine.font.DefaultFontFamily;
import io.karma.peregrine.font.DefaultFontRenderer;
import io.karma.peregrine.framebuffer.DefaultFramebufferBuilder;
import io.karma.peregrine.reload.DefaultReloadHandler;
import io.karma.peregrine.shader.BinaryShaderLoader;
import io.karma.peregrine.shader.DefaultShaderLoader;
import io.karma.peregrine.shader.DefaultShaderPreProcessor;
import io.karma.peregrine.shader.DefaultShaderProgramBuilder;
import io.karma.peregrine.state.DefaultBlendModeBuilder;
import io.karma.peregrine.state.DefaultRenderTypeBuilder;
import io.karma.peregrine.target.DefaultRenderTargetFactories;
import io.karma.peregrine.texture.DefaultTextureFactories;
import io.karma.peregrine.uniform.DefaultUniformTypeFactories;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Vector4f;
import org.lwjgl.opengl.ARBGetProgramBinary;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@Mod(Peregrine.MODID)
public final class PeregrineMod {
    @Internal
    public static final DefaultReloadHandler RELOAD_HANDLER = new DefaultReloadHandler();
    @Internal
    public static final DefaultDispositionHandler DISPOSE_HANDLER = new DefaultDispositionHandler();
    @Internal
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final DefaultUniformTypeFactories UNIFORM_TYPE_FACTORIES = new DefaultUniformTypeFactories();
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final DefaultTextureFactories TEXTURE_FACTORIES = new DefaultTextureFactories();
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final DefaultShaderPreProcessor SHADER_PRE_PROCESSOR = new DefaultShaderPreProcessor();
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final Lazy<ShaderLoader> SHADER_LOADER = Lazy.of(() -> {
        if (Peregrine.supportsBinaryShaderCaching()) {
            return new BinaryShaderLoader();
        }
        return new DefaultShaderLoader();
    });
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final DefaultRenderTargetFactories RENDER_TARGET_FACTORIES = new DefaultRenderTargetFactories();

    private static PeregrineMod instance;
    // @formatter:off
    @OnlyIn(Dist.CLIENT)
    private static final Lazy<UniformBuffer> GLOBAL_UNIFORMS = Lazy.of(() -> DefaultUniformBufferBuilder.build(it -> it
        .uniform("ProjMat", MatrixType.MAT4)
        .uniform("ModelViewMat", MatrixType.MAT4)
        .uniform("ColorModulator", VectorType.VEC4)
        .uniform("Time", ScalarType.FLOAT)
        .onBind((program, buffer) -> {
            final var cache = buffer.getCache();
            cache.getMat4("ProjMat").set(RenderSystem.getProjectionMatrix());
            cache.getMat4("ModelViewMat").set(RenderSystem.getModelViewMatrix());
            cache.getVec4("ColorModulator").set(new Vector4f(RenderSystem.getShaderColor()));
            cache.getFloat("Time").set(instance.renderTicks + instance.partialTicks);
        })
    ));
    // @formatter:on

    private static boolean isDevEnvironment;
    @OnlyIn(Dist.CLIENT)
    private static boolean isSodiumInstalled;
    @OnlyIn(Dist.CLIENT)
    private static boolean isIrisInstalled;

    @OnlyIn(Dist.CLIENT)
    private int renderTicks;
    @OnlyIn(Dist.CLIENT)
    private float partialTicks;

    public PeregrineMod() {
        instance = this;

        try {
            Class.forName("net.minecraft.world.level.Level");
            isDevEnvironment = true;
            Peregrine.LOGGER.info("Detected development environment");
        }
        catch (Throwable error) { /* SWALLOW */ }

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCreateRegistries);
        final var forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(this::onGameShutdown);

        // Initialize the API

        final var environment = new HashMap<String, Object>();
        environment.put("is_dev_environment", isDevEnvironment);
        final var di = new DI();
        di.put(FontFamilyFactory.class, DefaultFontFamily::new);
        di.put(Environment.class, new Environment(environment));

        DistExecutor.unsafeRunForDist(() -> () -> {
            final var modList = FMLLoader.getLoadingModList();
            isSodiumInstalled = modList.getModFileById("sodium") != null || modList.getModFileById("embeddium") != null;
            isIrisInstalled = modList.getModFileById("iris") != null || modList.getModFileById("oculus") != null;
            forgeBus.addListener(this::onClientTick);
            forgeBus.addListener(this::onRenderTick);
            ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(
                RELOAD_HANDLER);

            Minecraft.getInstance().execute(() -> {
                environment.put("is_sodium_installed", isSodiumInstalled);
                environment.put("is_iris_installed", isIrisInstalled);
                di.put(TextureFactories.class, TEXTURE_FACTORIES);
                di.put(UniformTypeFactories.class, UNIFORM_TYPE_FACTORIES);
                di.put(UniformBufferFactory.class, DefaultUniformBufferBuilder::build);
                di.put(ShaderProgramFactory.class, DefaultShaderProgramBuilder::build);
                di.put(FramebufferFactory.class, DefaultFramebufferBuilder::build);
                di.put(RenderTypeFactory.class, DefaultRenderTypeBuilder::build);
                di.put(BlendModeFactory.class, DefaultBlendModeBuilder::build);
                di.put(FontRendererFactory.class, DefaultFontRenderer::new);
                di.put(ShaderLoaderProvider.class, SHADER_LOADER::get);
                di.put(UniformBufferProvider.class, GLOBAL_UNIFORMS::get);
                di.put(ShaderPreProcessorProvider.class, () -> SHADER_PRE_PROCESSOR);
                di.put(RenderTargetFactories.class, RENDER_TARGET_FACTORIES);
                di.put(ShaderBinaryFormat.class, detectShaderBinaryFormat());

                Peregrine.init(EXECUTOR_SERVICE, RELOAD_HANDLER, DISPOSE_HANDLER, di);
            });

            return null;
        }, () -> () -> {
            Peregrine.init(EXECUTOR_SERVICE, RELOAD_HANDLER, DISPOSE_HANDLER, di);
            return null;
        });
    }

    public static PeregrineMod getInstance() {
        return instance;
    }

    public static boolean isDevelopmentEnvironment() {
        return isDevEnvironment;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isSodiumInstalled() {
        return isSodiumInstalled;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isIrisInstalled() {
        return isIrisInstalled;
    }

    @OnlyIn(Dist.CLIENT)
    public static UniformBuffer getGlobalUniforms() {
        return GLOBAL_UNIFORMS.get();
    }

    @OnlyIn(Dist.CLIENT)
    private static ShaderBinaryFormat detectShaderBinaryFormat() {
        if (GL.getCapabilities().GL_ARB_get_program_binary) {
            try (final var stack = MemoryStack.stackPush()) {
                final var formatCount = GL11.glGetInteger(ARBGetProgramBinary.GL_NUM_PROGRAM_BINARY_FORMATS);
                if (formatCount == 0) {
                    return ShaderBinaryFormat.NONE;
                }
                final var formats = stack.mallocInt(formatCount);
                GL11.glGetIntegerv(ARBGetProgramBinary.GL_PROGRAM_BINARY_FORMATS, formats);
                return new ShaderBinaryFormat(formats.get(0));
            }
        }
        else {
            return ShaderBinaryFormat.NONE;
        }
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    @OnlyIn(Dist.CLIENT)
    private void onClientTick(final ClientTickEvent event) {
        if (event.phase == Phase.END) {
            renderTicks++;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void onRenderTick(final RenderTickEvent event) {
        if (event.phase == Phase.START) {
            partialTicks = event.renderTickTime;
        }
    }

    private void onCreateRegistries(final NewRegistryEvent event) {
        Peregrine.LOGGER.info("Creating registries");
        event.create(RegistryBuilder.of(Peregrine.FONT_FAMILY_REGISTRY_NAME));
    }

    private void onGameShutdown(final GameShuttingDownEvent event) {
        try {
            EXECUTOR_SERVICE.shutdown();
            if (EXECUTOR_SERVICE.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR_SERVICE.shutdownNow().forEach(Runnable::run);
            }
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not shutdown executor service", error);
        }
    }
}
