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
import io.karma.peregrine.dispose.Disposable;
import io.karma.peregrine.reload.Reloadable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL13;

/**
 * A bindable texture object backed by GPU memory.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface Texture extends Reloadable, Disposable {
    /**
     * Creates a new texture object described by the
     * given properties.
     *
     * @param location The location of the texture to
     *                 load into the newly allocated texture object.
     * @return a new texture object which contains the pixel
     * data of the given image resource.
     */
    static Texture create(final ResourceLocation location) {
        return Peregrine.getTextureFactories().get(location);
    }

    /**
     * Retrieves the OpenGL ID of this texture object.
     * Should be used with care as its easy to mess up global state.
     * Also, the texture ID returned by this function may be
     * immutable when used in a static {@link io.karma.peregrine.shader.Sampler}.
     *
     * @return the OpenGL texture ID of this texture object.
     */
    int getId();

    /**
     * Binds this texture object to the current rendering context.
     * This will set the texture ID of the currently active
     * texture unit to the ID of this texture object.
     */
    void bind();

    /**
     * Binds this texture object to the current rendering context.
     * This will set the texture ID of the given texture unit
     * to the ID of this texture object.
     *
     * @param unit The index of the texture unit to bind to.
     */
    default void bind(final int unit) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        bind();
    }

    /**
     * Unbinds this texture object from the current rendering context.
     * This will reset the texture ID of the currently active
     * texture unit to 0.
     */
    void unbind();
}
