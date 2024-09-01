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

package io.karma.peregrine.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DSA {
    // @formatter:off
    private DSA() {}
    // @formatter:on

    public static int createTexture() {
        if (GL.getCapabilities().GL_ARB_direct_state_access) {
            return ARBDirectStateAccess.glCreateTextures(GL11.GL_TEXTURE_2D);
        }
        return GL11.glGenTextures();
    }

    public static void texImage2D(final int texture,
                                  final int level,
                                  final int border,
                                  final int width,
                                  final int height,
                                  final int internalFormat,
                                  final int format,
                                  final int type,
                                  final int[] data) {
        if (GL.getCapabilities().GL_ARB_direct_state_access) {
            ARBDirectStateAccess.glTextureStorage2D(texture, 1, internalFormat, width, height);
            if (data == null) {
                return;
            }
            ARBDirectStateAccess.glTextureSubImage2D(texture, level, border, border, width, height, format, type, data);
            return;
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, internalFormat, width, height, border, format, type, data);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public static void texParameteri(final int texture, final Consumer<IntBiConsumer> closure) {
        if (GL.getCapabilities().GL_ARB_direct_state_access) {
            closure.accept((n, v) -> ARBDirectStateAccess.glTextureParameteri(texture, n, v));
            return;
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        closure.accept((n, v) -> GL11.glTexParameteri(GL11.GL_TEXTURE_2D, n, v));
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public static int getTexParameteri(final int texture, final int param) {
        if (GL.getCapabilities().GL_ARB_direct_state_access) {
            return ARBDirectStateAccess.glGetTextureParameteri(texture, param);
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        final var result = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, param);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return result;
    }
}
