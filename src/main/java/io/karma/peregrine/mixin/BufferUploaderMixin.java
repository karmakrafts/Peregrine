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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import io.karma.peregrine.hooks.PeregrineRenderSystem;
import io.karma.peregrine.hooks.PeregrineVertexBuffer;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@Mixin(BufferUploader.class)
public final class BufferUploaderMixin {
    @Shadow
    private static @Nullable VertexBuffer upload(final RenderedBuffer pBuffer) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("all")
    @Inject(method = "_drawWithShader", at = @At("HEAD"), cancellable = true)
    private static void onDrawWithShader(final RenderedBuffer buffer, final CallbackInfo cbi) {
        final var uploadedBuffer = upload(buffer);
        if (uploadedBuffer == null) {
            cbi.cancel();
            return;
        }
        final var shader = PeregrineRenderSystem.getInstance().peregrine$getShader();
        if (shader instanceof ShaderInstance shaderInstance) {
            uploadedBuffer.drawWithShader(RenderSystem.getModelViewMatrix(),
                RenderSystem.getProjectionMatrix(),
                shaderInstance);
            cbi.cancel();
            return;
        }
        ((PeregrineVertexBuffer) uploadedBuffer).peregrine$drawWithShader(RenderSystem.getProjectionMatrix(),
            RenderSystem.getProjectionMatrix(),
            shader);
        cbi.cancel();
    }
}
