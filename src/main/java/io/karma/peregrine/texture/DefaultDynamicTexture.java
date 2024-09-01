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

import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.util.DSA;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultDynamicTexture implements DynamicTexture {
    private final int id;
    private TextureFormat format;

    public DefaultDynamicTexture(final TextureFilter minFilter,
                                 final TextureFilter magFilter,
                                 final TextureWrapMode horizontalWrapMode,
                                 final TextureWrapMode verticalWrapMode) {
        id = TextureUtils.createTexture(minFilter, magFilter, horizontalWrapMode, verticalWrapMode);
        PeregrineMod.DISPOSE_HANDLER.register(this);
    }

    @Override
    public void resize(final TextureFormat format, final int width, final int height) {
        DSA.texImage2D(id,
            0,
            0,
            width,
            height,
            format.getInternalGLType(),
            format.getGLType(),
            format.getGLDataType(),
            new int[0]);
    }

    @Override
    public TextureFormat getFormat() {
        return format;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    @Override
    public void unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    @Override
    public int getWidth() {
        return DSA.getTexParameteri(id, GL11.GL_TEXTURE_WIDTH);
    }

    @Override
    public int getHeight() {
        return DSA.getTexParameteri(id, GL11.GL_TEXTURE_HEIGHT);
    }

    @Override
    public TextureFilter getMinFilter() {
        return TextureFilter.fromGLType(DSA.getTexParameteri(id, GL11.GL_TEXTURE_MIN_FILTER));
    }

    @Override
    public TextureFilter getMagFilter() {
        return TextureFilter.fromGLType(DSA.getTexParameteri(id, GL11.GL_TEXTURE_MAG_FILTER));
    }

    @Override
    public TextureWrapMode getHorizontalWrapMode() {
        return TextureWrapMode.fromGLType(DSA.getTexParameteri(id, GL11.GL_TEXTURE_WRAP_S));
    }

    @Override
    public TextureWrapMode getVerticalWrapMode() {
        return TextureWrapMode.fromGLType(DSA.getTexParameteri(id, GL11.GL_TEXTURE_WRAP_T));
    }

    @Override
    public void dispose() {
        GL11.glDeleteTextures(id);
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
    }
}
