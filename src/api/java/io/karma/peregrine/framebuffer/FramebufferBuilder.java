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

package io.karma.peregrine.framebuffer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

/**
 * Describes a builder pattern for creating
 * new immutable framebuffer objects.
 * See {@link Framebuffer#create(Consumer)}.
 *
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface FramebufferBuilder {
    /**
     * Sets the width of the newly created framebuffer in pixels.
     *
     * @param width the width of the newly created framebuffer in pixels.
     * @return this builder instance.
     */
    FramebufferBuilder width(final int width);

    /**
     * Sets the height of the newly created framebuffer in pixels.
     *
     * @param height the height of the newly created framebuffer in pixels.
     * @return this builder instance.
     */
    FramebufferBuilder height(final int height);

    /**
     * Adds a new attachment to the newly created framebuffer
     * described by the given properties.
     *
     * @param callback a callback to describe the properties
     *                 of the attachment added to the newly
     *                 created framebuffer.
     * @return this builder instance.
     */
    FramebufferBuilder attachment(final Consumer<AttachmentBuilder> callback);

    /**
     * Adds a callback to the newly created framebuffer
     * which is invoked when the framebuffer is bound
     * as the current render target for OpenGL.
     *
     * @param callback the callback to add for when the newly
     *                 created framebuffer is bound.
     * @return this builder instance.
     */
    FramebufferBuilder onBind(final Consumer<Framebuffer> callback);

    /**
     * Adds a callback to the newly created framebuffer
     * which is invoked when the framebuffer is unbound
     * as the current render target for OpenGL.
     *
     * @param callback the callback to add for when the newly
     *                 created framebuffer is unbound.
     * @return this builder instance.
     */
    FramebufferBuilder onUnbind(final Consumer<Framebuffer> callback);
}
