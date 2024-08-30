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

import io.karma.peregrine.buffer.DefaultUniformBufferBuilder;
import io.karma.peregrine.buffer.UniformBuffer;
import io.karma.peregrine.buffer.UniformBufferFactory;
import io.karma.peregrine.buffer.UniformBufferProvider;
import io.karma.peregrine.dispose.DefaultDispositionHandler;
import io.karma.peregrine.reload.DefaultReloadHandler;
import io.karma.peregrine.shader.DefaultShaderLoader;
import io.karma.peregrine.shader.DefaultShaderProgramBuilder;
import io.karma.peregrine.shader.ShaderLoaderProvider;
import io.karma.peregrine.shader.ShaderProgramFactory;
import io.karma.peregrine.texture.DefaultTextureFactories;
import io.karma.peregrine.texture.TextureFactories;
import io.karma.peregrine.uniform.*;
import io.karma.peregrine.util.DI;
import io.karma.peregrine.util.ShaderBinaryFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.lwjgl.opengl.ARBGetProgramBinary;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

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
    public static final DefaultShaderLoader SHADER_LOADER = new DefaultShaderLoader();

    // @formatter:off
    @OnlyIn(Dist.CLIENT)
    private static final Lazy<UniformBuffer> GLOBAL_UNIFORMS = Lazy.of(() -> DefaultUniformBufferBuilder.build(it -> it
        .uniform("ProjMat", MatrixType.MAT4)
        .uniform("ModelViewMat", MatrixType.MAT4)
        .uniform("ColorModulator", VectorType.VEC4)
        .uniform("Time", ScalarType.FLOAT)));
    // @formatter:on

    public PeregrineMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCreateRegistries);
        MinecraftForge.EVENT_BUS.addListener(this::onGameShutdown);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(
                RELOAD_HANDLER);
        });
        Minecraft.getInstance().execute(() -> {
            final var di = new DI();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                di.put(TextureFactories.class, TEXTURE_FACTORIES);
                di.put(UniformTypeFactories.class, UNIFORM_TYPE_FACTORIES);
                di.put(UniformBufferFactory.class, DefaultUniformBufferBuilder::build);
                di.put(ShaderProgramFactory.class, DefaultShaderProgramBuilder::build);
                di.put(ShaderLoaderProvider.class, () -> SHADER_LOADER);
                di.put(UniformBufferProvider.class, GLOBAL_UNIFORMS::get);
                di.put(ShaderBinaryFormat.class, new ShaderBinaryFormat(detectShaderBinaryFormat()));
            });
            Peregrine.init(EXECUTOR_SERVICE, RELOAD_HANDLER, DISPOSE_HANDLER, di);
        });
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

    @OnlyIn(Dist.CLIENT)
    private static int detectShaderBinaryFormat() {
        if (GL.getCapabilities().GL_ARB_get_program_binary) {
            try (final var stack = MemoryStack.stackPush()) {
                final var formatCount = GL11.glGetInteger(ARBGetProgramBinary.GL_NUM_PROGRAM_BINARY_FORMATS);
                if (formatCount == 0) {
                    return -1;
                }
                final var formats = stack.mallocInt(formatCount);
                GL11.glGetIntegerv(ARBGetProgramBinary.GL_PROGRAM_BINARY_FORMATS, formats);
                return formats.get(0);
            }
        }
        else {
            return -1;
        }
    }
}
