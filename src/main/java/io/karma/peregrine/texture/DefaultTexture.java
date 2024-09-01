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

import io.karma.peregrine.Peregrine;
import io.karma.peregrine.PeregrineMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultTexture implements Texture {
    private static final SimpleTextureFormat FORMAT = SimpleTextureFormat.get(GL12.GL_BGRA,
        GL11.GL_RGBA8,
        GL12.GL_UNSIGNED_INT_8_8_8_8_REV);
    private final ResourceLocation location;
    private final TextureFilter minFilter;
    private final TextureFilter magFilter;
    private final TextureWrapMode horizontalWrapMode;
    private final TextureWrapMode verticalWrapMode;
    private int id = -1;
    private int width;
    private int height;

    public DefaultTexture(final ResourceLocation location,
                          final TextureFilter minFilter,
                          final TextureFilter magFilter,
                          final TextureWrapMode horizontalWrapMode,
                          final TextureWrapMode verticalWrapMode) {
        this.location = location;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.horizontalWrapMode = horizontalWrapMode;
        this.verticalWrapMode = verticalWrapMode;
        PeregrineMod.DISPOSE_HANDLER.register(this);
        PeregrineMod.RELOAD_HANDLER.register(this);
    }

    @Override
    public TextureFormat getFormat() {
        return FORMAT;
    }

    @Override
    public int getId() {
        return id;
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
    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    @Override
    public void unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    @Override
    public void dispose() {
        GL11.glDeleteTextures(id);
    }

    @Override
    public void prepare(final ResourceProvider resourceProvider) {
        if (id != -1) {
            GL11.glDeleteTextures(id);
        }
        id = TextureUtils.createTexture(minFilter, magFilter, horizontalWrapMode, verticalWrapMode);
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
        try (final var stream = resourceProvider.open(location)) {
            final var image = ImageIO.read(stream);
            final var width = image.getWidth();
            final var height = image.getHeight();

            final var maxSize = Peregrine.getMaxTextureSize();
            if (width > maxSize || height > maxSize) {
                Peregrine.LOGGER.error("Texture {} exceeds maximum size, skipping reload", location);
                return;
            }
            this.width = width;
            this.height = height;

            TextureUtils.uploadTexture(id, image);
            Peregrine.LOGGER.debug("Uploaded image {} to texture {}", location, id);
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not read static texture", error);
        }
    }

    @Override
    public String toString() {
        return String.format("StaticTexture[%s]", location);
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DefaultTexture texture)) {
            return false;
        }
        return location.equals(texture.location);
    }
}
