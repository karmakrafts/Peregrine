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

import io.karma.peregrine.texture.TextureType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL30;

/**
 * A list of all supported attachments on a single
 * framebuffer object.
 *
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public enum AttachmentType {
    // @formatter:off
    COLOR        (TextureType.COLOR,         GL30.GL_COLOR_ATTACHMENT0),
    COLOR_1      (TextureType.COLOR,         GL30.GL_COLOR_ATTACHMENT1),
    COLOR_2      (TextureType.COLOR,         GL30.GL_COLOR_ATTACHMENT2),
    COLOR_3      (TextureType.COLOR,         GL30.GL_COLOR_ATTACHMENT3),
    COLOR_4      (TextureType.COLOR,         GL30.GL_COLOR_ATTACHMENT4),
    COLOR_5      (TextureType.COLOR,         GL30.GL_COLOR_ATTACHMENT5),
    COLOR_6      (TextureType.COLOR,         GL30.GL_COLOR_ATTACHMENT6),
    COLOR_7      (TextureType.COLOR,         GL30.GL_COLOR_ATTACHMENT7),
    DEPTH        (TextureType.DEPTH,         GL30.GL_DEPTH_ATTACHMENT),
    STENCIL      (TextureType.STENCIL,       GL30.GL_STENCIL_ATTACHMENT),
    DEPTH_STENCIL(TextureType.DEPTH_STENCIL, GL30.GL_DEPTH_STENCIL_ATTACHMENT);
    // @formatter:on

    private final TextureType textureType;
    private final int glType;

    AttachmentType(final TextureType textureType, final int glType) {
        this.textureType = textureType;
        this.glType = glType;
    }

    /**
     * Retrieves the texture type used for this attachment.
     *
     * @return the texture type used for this attachment.
     */
    public TextureType getTextureType() {
        return textureType;
    }

    /**
     * Retrieves the internal OpenGL representation
     * of this attachment.
     *
     * @return the internal OpenGL representation
     * of this attachment.
     */
    public int getGLType() {
        return glType;
    }
}
