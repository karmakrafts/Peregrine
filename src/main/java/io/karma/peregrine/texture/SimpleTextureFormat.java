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

package io.karma.peregrine.texture;

import io.karma.peregrine.api.texture.TextureFormat;
import io.karma.peregrine.api.texture.TextureType;
import io.karma.peregrine.api.util.HashUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class SimpleTextureFormat implements TextureFormat {
    private static final Int2ObjectOpenHashMap<SimpleTextureFormat> CACHE = new Int2ObjectOpenHashMap<>();

    private final int glType;
    private final int internalGlType;
    private final int glDataType;
    private final TextureType type;

    private SimpleTextureFormat(final int glType, final int internalGlType, final int glDataType) {
        this.glType = glType;
        this.internalGlType = internalGlType;
        this.glDataType = glDataType;
        type = guessType(glType);
    }

    public static SimpleTextureFormat get(final int glType, final int internalGlType, final int glDataType) {
        return CACHE.computeIfAbsent(HashUtils.combine(Integer.hashCode(glType), internalGlType, glDataType),
            hash -> new SimpleTextureFormat(glType, internalGlType, glDataType));
    }

    private static TextureType guessType(final int glType) {
        return switch (glType) {
            case GL11.GL_DEPTH_COMPONENT -> TextureType.DEPTH;
            case GL11.GL_STENCIL_INDEX -> TextureType.STENCIL;
            case GL30.GL_DEPTH_STENCIL -> TextureType.DEPTH_STENCIL;
            default -> TextureType.COLOR;
        };
    }

    @Override
    public TextureType getType() {
        return type;
    }

    @Override
    public int getGLType() {
        return glType;
    }

    @Override
    public int getInternalGLType() {
        return internalGlType;
    }

    @Override
    public int getGLDataType() {
        return glDataType;
    }
}
