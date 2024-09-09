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

package io.karma.peregrine.mixin;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.hooks.PeregrineRenderSystem;
import io.karma.peregrine.hooks.PeregrineShader;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin implements PeregrineRenderSystem {
    @SuppressWarnings("all")
    private static RenderSystem peregrine$instance; // This is non-unique on purpose since we reflect this
    @Unique
    private PeregrineShader peregrine$shader;

    @Shadow
    public static boolean isOnRenderThread() {
        throw new UnsupportedOperationException();
    }

    @Shadow
    public static void recordRenderCall(final RenderCall call) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("all")
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClInit(final CallbackInfo cbi) {
        new RenderSystem();
    }

    @Inject(method = "setShader", at = @At("HEAD"))
    private static void onSetShader(final Supplier<ShaderInstance> shader, final CallbackInfo cbi) {
        if (isOnRenderThread()) {
            RenderSystemMixin.class.cast(peregrine$instance).peregrine$shader = (PeregrineShader) shader.get();
        }
        else {
            recordRenderCall(() -> {
                RenderSystemMixin.class.cast(peregrine$instance).peregrine$shader = (PeregrineShader) shader.get();
            });
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(final CallbackInfo cbi) {
        peregrine$instance = RenderSystem.class.cast(this);
        Peregrine.LOGGER.debug("Created extended RenderSystem instance");
    }

    @Override
    public PeregrineShader peregrine$getShader() {
        return peregrine$shader;
    }

    @Override
    public void peregrine$setShader(final PeregrineShader shader) {
        peregrine$shader = shader;
    }
}
