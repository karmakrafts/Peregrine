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

import io.karma.peregrine.api.state.DSA;
import io.karma.peregrine.api.texture.*;
import io.karma.peregrine.api.util.TextureUtils;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class ProxyTexture implements Texture {
    private final int width;
    private final int height;
    private final SimpleTextureFormat format;
    private final TextureFilter minFilter;
    private final TextureFilter magFilter;
    private final TextureWrapMode horizontalWrapMode;
    private final TextureWrapMode verticalWrapMode;
    private int textureId;

    public ProxyTexture(final int textureId) {
        this.textureId = textureId;
        width = DSA.getTexParameteri(textureId, GL11.GL_TEXTURE_WIDTH);
        height = DSA.getTexParameteri(textureId, GL11.GL_TEXTURE_HEIGHT);
        final var format = DSA.getTexParameteri(textureId, GL11.GL_TEXTURE_INTERNAL_FORMAT);
        this.format = SimpleTextureFormat.get(format, format, TextureUtils.guessStorageType(format));
        minFilter = TextureFilter.fromGLType(DSA.getTexParameteri(textureId, GL11.GL_TEXTURE_MIN_FILTER));
        magFilter = TextureFilter.fromGLType(DSA.getTexParameteri(textureId, GL11.GL_TEXTURE_MAG_FILTER));
        horizontalWrapMode = TextureWrapMode.fromGLType(DSA.getTexParameteri(textureId, GL11.GL_TEXTURE_WRAP_S));
        verticalWrapMode = TextureWrapMode.fromGLType(DSA.getTexParameteri(textureId, GL11.GL_TEXTURE_WRAP_T));
    }

    @Override
    public int getId() {
        return textureId;
    }

    @Override
    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
    }

    @Override
    public void unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public TextureFilter getMinFilter() {
        return minFilter;
    }

    @Override
    public TextureFilter getMagFilter() {
        return magFilter;
    }

    @Override
    public TextureWrapMode getHorizontalWrapMode() {
        return horizontalWrapMode;
    }

    @Override
    public TextureWrapMode getVerticalWrapMode() {
        return verticalWrapMode;
    }

    @Override
    public TextureFormat getFormat() {
        return format;
    }

    @Override
    public void dispose() {
        textureId = INVALID_ID;
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
        // We don't own this texture
    }
}
