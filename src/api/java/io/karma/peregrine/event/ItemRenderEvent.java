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
 * An event which can be used to implement entirely custom
 * rendered items, without using janky block entity renderer hacks.
 * Provides a cancellable pre and a post stage.
 *
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

    /**
     * Retrieves the partial tick of the previous frame.
     *
     * @return the partial tick of the previous frame.
     */
    public float getFrameTime() {
        return frameTime;
    }

    /**
     * Retrieves the hand in which the rendered item is being held
     * by the player.
     *
     * @return the hand in which the rendered item is being held.
     */
    public InteractionHand getHand() {
        return isLeftHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    /**
     * Determines whether the rendered item is being held in the
     * left hand of the player.
     *
     * @return True if the rendered item resides in the players left hand.
     */
    public boolean isLeftHand() {
        return isLeftHand;
    }

    /**
     * Retrieves the stack of the item being rendered.
     *
     * @return the stack of the item being rendered.
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * Retrieves the display context of the item being rendered.
     * This can be used to determine the perspective in which
     * the item is being rendered. See all values of {@link ItemDisplayContext}.
     *
     * @return the display context of the item being rendered.
     */
    public ItemDisplayContext getDisplayContext() {
        return displayContext;
    }

    /**
     * Retrieves the matrix stack of the current rendering context.
     * This stack contains the transforms being applied to the item locally.
     *
     * @return the matrix stack of the current rendering context.
     */
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * Retrieves the buffer source used for rendering items in the
     * current rendering context. May be used with custom {@link net.minecraft.client.renderer.RenderType}s.
     *
     * @return the buffer source used for rendering items in the current rendering context.
     */
    public MultiBufferSource getBufferSource() {
        return bufferSource;
    }

    /**
     * Retrieves the packed light map coordinates in
     * the format described by {@link net.minecraft.client.renderer.LightTexture}.
     *
     * @return the packed light map coordinates.
     */
    public int getPackedLight() {
        return packedLight;
    }

    /**
     * Retrieves the packed overlay map coordinates in
     * the format described by {@link net.minecraft.client.renderer.texture.OverlayTexture}.
     *
     * @return the packed overlay map coordinates.
     */
    public int getPackedOverlay() {
        return packedOverlay;
    }

    /**
     * The pre-stage of the {@link ItemRenderEvent}.
     * This stage allows cancelling the vanilla-rendering of an item entirely.
     * Useful for replacing the rendering behaviour of an item entirely.
     * Should be used with care as it can easily break global GL state.
     */
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

    /**
     * The post-stage of the {@link ItemRenderEvent}.
     * This stage allows rendering extras to an existing item, like particles
     * or other effects. This event is not cancellable.
     */
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
