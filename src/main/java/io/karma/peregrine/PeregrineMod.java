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
import io.karma.peregrine.dispose.DefaultDispositionHandler;
import io.karma.peregrine.reload.DefaultReloadHandler;
import io.karma.peregrine.shader.DefaultShaderLoader;
import io.karma.peregrine.shader.DefaultShaderProgramBuilder;
import io.karma.peregrine.texture.DefaultTextureFactories;
import io.karma.peregrine.uniform.DefaultUniformTypeFactories;
import io.karma.peregrine.uniform.MatrixType;
import io.karma.peregrine.uniform.ScalarType;
import io.karma.peregrine.uniform.VectorType;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@Mod(Peregrine.MODID)
public final class PeregrineMod {
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final DefaultReloadHandler RELOAD_HANDLER = new DefaultReloadHandler();
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final DefaultDispositionHandler DISPOSE_HANDLER = new DefaultDispositionHandler();
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final DefaultUniformTypeFactories UNIFORM_TYPE_FACTORIES = new DefaultUniformTypeFactories();
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final DefaultTextureFactories TEXTURE_FACTORIES = new DefaultTextureFactories();
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final DefaultShaderLoader SHADER_LOADER = new DefaultShaderLoader();
    @OnlyIn(Dist.CLIENT)
    @Internal
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    // @formatter:off
    @OnlyIn(Dist.CLIENT)
    private static final Lazy<UniformBuffer> GLOBAL_UNIFORMS = Lazy.of(() -> DefaultUniformBufferBuilder.build(it -> it
        .uniform("ProjMat", MatrixType.MAT4)
        .uniform("ModelViewMat", MatrixType.MAT4)
        .uniform("ColorModulator", VectorType.VEC4)
        .uniform("Time", ScalarType.FLOAT)));
    // @formatter:on

    public PeregrineMod() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.addListener(this::onClientShutdown);
            ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(
                RELOAD_HANDLER);
            Minecraft.getInstance().execute(() -> {
                Peregrine.init(EXECUTOR_SERVICE,
                    RELOAD_HANDLER,
                    DISPOSE_HANDLER,
                    UNIFORM_TYPE_FACTORIES,
                    TEXTURE_FACTORIES,
                    DefaultUniformBufferBuilder::build,
                    DefaultShaderProgramBuilder::build,
                    () -> SHADER_LOADER,
                    GLOBAL_UNIFORMS);
            });
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void onClientShutdown(final GameShuttingDownEvent event) {
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
