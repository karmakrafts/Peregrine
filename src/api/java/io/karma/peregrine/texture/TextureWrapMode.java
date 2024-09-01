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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public enum TextureWrapMode {
    // @formatter:off
    CLAMP   (GL11.GL_CLAMP),
    REPEAT  (GL11.GL_REPEAT);
    // @formatter:on

    private final int glType;

    TextureWrapMode(final int glType) {
        this.glType = glType;
    }

    public static TextureWrapMode fromGLType(final int glType) {
        return switch (glType) {
            case GL11.GL_CLAMP -> CLAMP;
            case GL11.GL_REPEAT -> REPEAT;
            default -> throw new IllegalArgumentException("Unsupported texture wrap mode");
        };
    }

    public int getGLType() {
        return glType;
    }
}
