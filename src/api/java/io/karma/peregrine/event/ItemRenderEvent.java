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

package io.karma.peregrine.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public class ItemRenderEvent extends Event {
    private final ItemStack stack;
    private final ItemDisplayContext displayContext;
    private final boolean isLeftHand;
    private final PoseStack poseStack;
    private final MultiBufferSource bufferSource;
    private final int packedLight;
    private final int packedOverlay;
    private final float frameTime;

    protected ItemRenderEvent(final ItemStack stack,
                              final ItemDisplayContext displayContext,
                              final boolean isLeftHand,
                              final PoseStack poseStack,
                              final MultiBufferSource bufferSource,
                              final int packedLight,
                              final int packedOverlay,
                              final float frameTime) {
        this.stack = stack;
        this.displayContext = displayContext;
        this.isLeftHand = isLeftHand;
        this.poseStack = poseStack;
        this.bufferSource = bufferSource;
        this.packedLight = packedLight;
        this.packedOverlay = packedOverlay;
        this.frameTime = frameTime;
    }

    public float getFrameTime() {
        return frameTime;
    }

    public InteractionHand getHand() {
        return isLeftHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    public boolean isLeftHand() {
        return isLeftHand;
    }

    public ItemStack getStack() {
        return stack;
    }

    public ItemDisplayContext getDisplayContext() {
        return displayContext;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getBufferSource() {
        return bufferSource;
    }

    public int getPackedLight() {
        return packedLight;
    }

    public int getPackedOverlay() {
        return packedOverlay;
    }

    @Cancelable
    public static final class Pre extends ItemRenderEvent {
        public Pre(final ItemStack stack,
                   final ItemDisplayContext displayContext,
                   final boolean isLeftHand,
                   final PoseStack poseStack,
                   final MultiBufferSource bufferSource,
                   final int packedLight,
                   final int packedOverlay,
                   final float partialTick) {
            super(stack, displayContext, isLeftHand, poseStack, bufferSource, packedLight, packedOverlay, partialTick);
        }
    }

    public static final class Post extends ItemRenderEvent {
        public Post(final ItemStack stack,
                    final ItemDisplayContext displayContext,
                    final boolean isLeftHand,
                    final PoseStack poseStack,
                    final MultiBufferSource bufferSource,
                    final int packedLight,
                    final int packedOverlay,
                    final float partialTick) {
            super(stack, displayContext, isLeftHand, poseStack, bufferSource, packedLight, packedOverlay, partialTick);
        }
    }

}
