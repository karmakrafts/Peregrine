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
import io.karma.peregrine.util.TextureUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class StaticTexture implements Texture {
    private final ResourceLocation location;
    private int id = -1;

    public StaticTexture(final ResourceLocation location) {
        this.location = location;
        PeregrineMod.DISPOSE_HANDLER.register(this);
        PeregrineMod.RELOAD_HANDLER.register(this);
    }

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
    public void dispose() {
        GL11.glDeleteTextures(id);
    }

    @Override
    public void prepare(final ResourceProvider resourceProvider) {
        if (id != -1) {
            GL11.glDeleteTextures(id);
        }
        id = TextureUtils.createDefaultTexture();
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
        try (final var stream = resourceProvider.open(location)) {
            TextureUtils.uploadTexture(id, ImageIO.read(stream));
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
        if (!(obj instanceof StaticTexture texture)) {
            return false;
        }
        return location.equals(texture.location);
    }
}
