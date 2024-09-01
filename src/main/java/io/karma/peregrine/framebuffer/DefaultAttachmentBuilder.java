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

/**
 * @author Alexander Hinze
 * @since 01/09/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultAttachmentBuilder implements AttachmentBuilder {
    private final int width;
    private final int height;
    private AttachmentType type = AttachmentType.COLOR;
    private TextureFormat format;
    private TextureFilter minFilter = TextureFilter.NEAREST;
    private TextureFilter magFilter = TextureFilter.NEAREST;
    private TextureWrapMode horizontalWrapMode = TextureWrapMode.CLAMP;
    private TextureWrapMode verticalWrapMode = TextureWrapMode.CLAMP;

    DefaultAttachmentBuilder(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public DefaultAttachment build() {
        return new DefaultAttachment(width,
            height,
            type,
            format,
            minFilter,
            magFilter,
            horizontalWrapMode,
            verticalWrapMode);
    }

    @Override
    public AttachmentBuilder type(final AttachmentType type) {
        this.type = type;
        return this;
    }

    @Override
    public AttachmentBuilder format(final TextureFormat format) {
        this.format = format;
        return this;
    }

    @Override
    public AttachmentBuilder minFilter(final TextureFilter minFilter) {
        this.minFilter = minFilter;
        return this;
    }

    @Override
    public AttachmentBuilder magFilter(final TextureFilter magFilter) {
        this.magFilter = magFilter;
        return this;
    }

    @Override
    public AttachmentBuilder horizontalWrapMode(final TextureWrapMode horizontalWrapMode) {
        this.horizontalWrapMode = horizontalWrapMode;
        return this;
    }

    @Override
    public AttachmentBuilder verticalWrapMode(final TextureWrapMode verticalWrapMode) {
        this.verticalWrapMode = verticalWrapMode;
        return this;
    }
}
