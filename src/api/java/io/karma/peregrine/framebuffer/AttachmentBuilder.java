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

import io.karma.peregrine.texture.TextureFilter;
import io.karma.peregrine.texture.TextureFormat;
import io.karma.peregrine.texture.TextureWrapMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

/**
 * Describes a builder pattern to create new immutable
 * framebuffer attachment objects.
 * See {@link FramebufferBuilder#attachment(Consumer)}.
 *
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface AttachmentBuilder {
    /**
     * Sets the type of the newly created framebuffer attachment.
     * See {@link AttachmentType}.
     *
     * @param type the type of the newly created framebuffer attachment.
     * @return this builder instance.
     */
    AttachmentBuilder type(final AttachmentType type);

    /**
     * Sets the format of the backing texture of the newly
     * created framebuffer attachment.
     * See {@link io.karma.peregrine.texture.DefaultTextureFormat}.
     *
     * @param format the format of the newly created framebuffer attachment.
     * @return this builder instance.
     */
    AttachmentBuilder format(final TextureFormat format);

    /**
     * Sets the minifying filter of the backing texture
     * of the newly created framebuffer attachment.
     *
     * @param minFilter The minifying filter of the newly created framebuffer
     * @return this builder instance.
     */
    AttachmentBuilder minFilter(final TextureFilter minFilter);

    /**
     * Sets the magnifying filter of the backing texture
     * of the newly created framebuffer attachment.
     *
     * @param magFilter The magnifying filter of the newly created framebuffer
     * @return this builder instance.
     */
    AttachmentBuilder magFilter(final TextureFilter magFilter);

    /**
     * Sets the horizontal wrapping mode of the backing texture
     * of the newly created framebuffer attachment.
     *
     * @param horizontalWrapMode the horizontal wrapping mode of the backing texture
     *                           of the newly created framebuffer attachment.
     * @return this builder instance.
     */
    AttachmentBuilder horizontalWrapMode(final TextureWrapMode horizontalWrapMode);

    /**
     * Sets the vertical wrapping mode of the backing texture
     * of the newly created framebuffer attachment.
     *
     * @param verticalWrapMode the vertical wrapping mode of the backing texture
     *                         of the newly created framebuffer attachment.
     * @return this builder instance.
     */
    AttachmentBuilder verticalWrapMode(final TextureWrapMode verticalWrapMode);
}
