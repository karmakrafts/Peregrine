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

import io.karma.peregrine.texture.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultAttachment implements Attachment {
    private final AttachmentType type;
    private final DynamicTexture texture;

    public DefaultAttachment(final int width,
                             final int height,
                             final AttachmentType type,
                             final TextureFormat format,
                             final TextureFilter minFilter,
                             final TextureFilter magFilter,
                             final TextureWrapMode horizontalWrapMode,
                             final TextureWrapMode verticalWrapMode) {
        this.type = type;
        this.texture = new DefaultDynamicTexture(minFilter, magFilter, horizontalWrapMode, verticalWrapMode);
        texture.resize(format, width, height);
    }

    @Override
    public AttachmentType getType() {
        return type;
    }

    @Override
    public DynamicTexture getTexture() {
        return texture;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
