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

package io.karma.peregrine.api.framebuffer;

import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.api.dispose.Disposable;
import io.karma.peregrine.api.target.RenderTarget;
import io.karma.peregrine.api.texture.DynamicTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a framebuffer object along its
 * attachments in form of {@link DynamicTexture}s.
 *
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface Framebuffer extends RenderTarget, Disposable {
    int INVALID_ID = -1;

    /**
     * Creates a new framebuffer object described by the given properties.
     *
     * @param callback the callback to describe the properties
     *                 of the newly created framebuffer object.
     * @return a new framebuffer object with the given properties.
     */
    static Framebuffer create(final Consumer<FramebufferBuilder> callback) {
        return Peregrine.getFramebufferFactory().apply(callback);
    }

    /**
     * Retrieves the attachment of the given type
     * from this framebuffer if present.
     *
     * @param attachment the type of the attachment to retrieve.
     * @return the attachment of the given type if present.
     */
    @Nullable
    Attachment getAttachment(final AttachmentType attachment);

    /**
     * Retrieves a list of all attachments attached
     * to this framebuffer object in no particular order.
     *
     * @return a list of all attachments attached to this
     * framebuffer object.
     */
    List<Attachment> getAttachments();

    /**
     * Determines whether the given attachment type
     * is present on this framebuffer object.
     *
     * @param attachment the attachment type to check for.
     * @return true if the given attachment type
     * is present on this framebuffer object.
     */
    boolean hasAttachment(final AttachmentType attachment);

    /**
     * Resize this framebuffer to the given width and
     * height in pixels. The given dimensions should
     * ideally be a power of two.
     *
     * @param width  the new width of this framebuffer in pixels.
     * @param height the new height of this framebuffer in pixels.
     */
    void resize(final int width, final int height);

    /**
     * Clear this framebuffer to the given color.
     *
     * @param r the amount of red in the clear color.
     * @param g the amount of green in the clear color.
     * @param b the amount of blue in the clear color.
     * @param a the amount of opacity in the clear color.
     */
    void clear(final float r, final float g, final float b, final float a);

    /**
     * Retrieves the width of this framebuffer in pixels.
     *
     * @return the width of this framebuffer in pixels.
     */
    int getWidth();

    /**
     * Retrieves the height of this framebuffer in pixels.
     *
     * @return the height of this framebuffer in pixels.
     */
    int getHeight();

    /**
     * Determines whether this framebuffer has some type of depth attachment.
     * This includes {@code DEPTH} and {@code DEPTH_STENCIL} type attachments.
     *
     * @return true if this framebuffer has a depth attachment.
     */
    default boolean hasDepthAttachment() {
        return getAttachments().stream().anyMatch(att -> {
            final var type = att.getType();
            return type == AttachmentType.DEPTH || type == AttachmentType.DEPTH_STENCIL;
        });
    }

    /**
     * Determines whether this framebuffer has some type of stencil attachment.
     * This includes {@code STENCIL} and {@code DEPTH_STENCIL} type attachments.
     *
     * @return true if this framebuffer has a stencil attachment.
     */
    default boolean hasStencilAttachment() {
        return getAttachments().stream().anyMatch(att -> {
            final var type = att.getType();
            return type == AttachmentType.STENCIL || type == AttachmentType.DEPTH_STENCIL;
        });
    }
}
