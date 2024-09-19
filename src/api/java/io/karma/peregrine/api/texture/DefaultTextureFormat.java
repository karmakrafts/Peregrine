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

package io.karma.peregrine.api.texture;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public enum DefaultTextureFormat implements TextureFormat {
    // @formatter:off
    RGBA8             (TextureType.COLOR,         GL12.GL_RGBA8,             GL11.GL_RGBA,            GL12.GL_UNSIGNED_INT_8_8_8_8),
    RGBA8_REV         (TextureType.COLOR,         GL12.GL_RGBA8,             GL12.GL_BGRA,            GL12.GL_UNSIGNED_INT_8_8_8_8_REV),
    RGBA32F           (TextureType.COLOR,         GL30.GL_RGBA32F,           GL11.GL_RGBA,            GL11.GL_FLOAT),
    DEPTH_16          (TextureType.DEPTH,         GL30.GL_DEPTH_COMPONENT16, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT),
    DEPTH_24          (TextureType.DEPTH,         GL30.GL_DEPTH_COMPONENT24, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT),
    DEPTH_32          (TextureType.DEPTH,         GL30.GL_DEPTH_COMPONENT32, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT),
    STENCIL_1         (TextureType.STENCIL,       GL30.GL_STENCIL_INDEX1,    GL11.GL_STENCIL_INDEX,   GL11.GL_UNSIGNED_BYTE),
    STENCIL_8         (TextureType.STENCIL,       GL30.GL_STENCIL_INDEX8,    GL11.GL_STENCIL_INDEX,   GL11.GL_UNSIGNED_BYTE),
    STENCIL_16        (TextureType.STENCIL,       GL30.GL_STENCIL_INDEX16,   GL11.GL_STENCIL_INDEX,   GL11.GL_UNSIGNED_SHORT),
    DEPTH_24_STENCIL_8(TextureType.DEPTH_STENCIL, GL30.GL_DEPTH24_STENCIL8,  GL30.GL_DEPTH_STENCIL,   GL11.GL_UNSIGNED_INT);
    // @formatter:on

    private final TextureType type;
    private final int internalGLType;
    private final int glType;
    private final int glDataType;

    DefaultTextureFormat(final TextureType type, final int internalGLType, final int glType, final int glDataType) {
        this.type = type;
        this.internalGLType = internalGLType;
        this.glType = glType;
        this.glDataType = glDataType;
    }

    @Override
    public TextureType getType() {
        return type;
    }

    @Override
    public int getInternalGLType() {
        return internalGLType;
    }

    @Override
    public int getGLType() {
        return glType;
    }

    @Override
    public int getGLDataType() {
        return glDataType;
    }
}
