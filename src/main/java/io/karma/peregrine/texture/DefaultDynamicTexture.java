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
public final class DefaultDynamicTexture implements DynamicTexture {
    private int id = -1;
    private final TextureFormat format;
    private final TextureFilter minFilter;
    private final TextureFilter magFilter;
    private final TextureWrapMode horizontalWrapMode;
    private final TextureWrapMode verticalWrapMode;

    public DefaultDynamicTexture(final TextureFormat format,
                                 final TextureFilter minFilter,
                                 final TextureFilter magFilter,
                                 final TextureWrapMode horizontalWrapMode,
                                 final TextureWrapMode verticalWrapMode) {
        this.format = format;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.horizontalWrapMode = horizontalWrapMode;
        this.verticalWrapMode = verticalWrapMode;
        PeregrineMod.DISPOSE_HANDLER.register(this);
    }

    @Override
    public void resize(final int width, final int height) {
        dispose();
        id = TextureUtils.createTexture(minFilter, magFilter, horizontalWrapMode, verticalWrapMode);
        DSA.texImage2D(id,
            0,
            width,
            height,
            format.getInternalGLType(),
            format.getGLType(),
            format.getGLDataType(),
            null);
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
        if (id == -1) {
            return;
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    @Override
    public void unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    @Override
    public int getWidth() {
        if (id == -1) {
            return 0;
        }
        return DSA.getTexParameteri(id, GL11.GL_TEXTURE_WIDTH);
    }

    @Override
    public int getHeight() {
        if (id == -1) {
            return 0;
        }
        return DSA.getTexParameteri(id, GL11.GL_TEXTURE_HEIGHT);
    }

    @Override
    public TextureFilter getMinFilter() {
        if (id == -1) {
            return TextureFilter.NEAREST;
        }
        return TextureFilter.fromGLType(DSA.getTexParameteri(id, GL11.GL_TEXTURE_MIN_FILTER));
    }

    @Override
    public TextureFilter getMagFilter() {
        if (id == -1) {
            return TextureFilter.NEAREST;
        }
        return TextureFilter.fromGLType(DSA.getTexParameteri(id, GL11.GL_TEXTURE_MAG_FILTER));
    }

    @Override
    public TextureWrapMode getHorizontalWrapMode() {
        if (id == -1) {
            return TextureWrapMode.CLAMP;
        }
        return TextureWrapMode.fromGLType(DSA.getTexParameteri(id, GL11.GL_TEXTURE_WRAP_S));
    }

    @Override
    public TextureWrapMode getVerticalWrapMode() {
        if (id == -1) {
            return TextureWrapMode.CLAMP;
        }
        return TextureWrapMode.fromGLType(DSA.getTexParameteri(id, GL11.GL_TEXTURE_WRAP_T));
    }

    @Override
    public void dispose() {
        if (id == -1) {
            return;
        }
        GL11.glDeleteTextures(id);
        id = -1;
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
    }

    @Override
    public String toString() {
        return String.format("DefaultDynamicTexture[id=%d,format=%s]", id, format);
    }
}
