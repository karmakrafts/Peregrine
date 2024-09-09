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

import com.mojang.blaze3d.vertex.PoseStack;
import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.api.event.ItemRenderEvent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@Mixin(ItemRenderer.class)
public final class ItemRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderByItemPre(final ItemStack stack,
                                   final ItemDisplayContext displayContext,
                                   final boolean isLeftHand,
                                   final PoseStack poseStack,
                                   final MultiBufferSource bufferSource,
                                   final int packedLight,
                                   final int packedOverlay,
                                   final BakedModel model,
                                   final CallbackInfo cbi) {
        // @formatter:off
        final var event = new ItemRenderEvent.Pre(stack, displayContext, isLeftHand, poseStack, bufferSource,
            packedLight, packedOverlay, PeregrineMod.getInstance().getPartialTicks());
        // @formatter:on
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            cbi.cancel(); // Apply cancel flag from event to control-flow
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderByItemPost(final ItemStack stack,
                                    final ItemDisplayContext displayContext,
                                    final boolean isLeftHand,
                                    final PoseStack poseStack,
                                    final MultiBufferSource bufferSource,
                                    final int packedLight,
                                    final int packedOverlay,
                                    final BakedModel model,
                                    final CallbackInfo cbi) {
        // @formatter:off
        MinecraftForge.EVENT_BUS.post(new ItemRenderEvent.Post(stack, displayContext, isLeftHand, poseStack,
            bufferSource, packedLight, packedOverlay, PeregrineMod.getInstance().getPartialTicks()));
        // @formatter:on
    }
}


