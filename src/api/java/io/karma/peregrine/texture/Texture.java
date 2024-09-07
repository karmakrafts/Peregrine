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

import java.util.function.IntSupplier;

/**
 * A bindable texture object backed by GPU memory.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface Texture extends Reloadable, Disposable, IntSupplier {
    int INVALID_ID = -1;

    /**
     * Creates a new empty texture object with the given properties.
     *
     * @param minFilter          the minifying texture filter applied to the texture.
     * @param magFilter          the magnifying texture filter applied to the texture.
     * @param horizontalWrapMode the type of wrapping applied to the texture
     *                           horizontally when it is sampled beyond its bounds.
     * @param verticalWrapMode   the type of wrapping applied to the texture
     *                           vertically when it is sampled beyond its bounds.
     * @return a new empty texture object with the given properties.
     */
    static Texture create(final TextureFilter minFilter,
                          final TextureFilter magFilter,
                          final TextureWrapMode horizontalWrapMode,
                          final TextureWrapMode verticalWrapMode) {
        return Peregrine.getTextureFactories().create(minFilter, magFilter, horizontalWrapMode, verticalWrapMode);
    }

    /**
     * Retrieves a static texture object which contains
     * the image data of the given texture resource.
     * Creates a new static texture if none has been
     * created for the given resource location.
     *
     * @param location           the location of the image resource to load
     *                           into the newly created texture object.
     * @param minFilter          the minifying texture filter applied to the texture.
     * @param magFilter          the magnifying texture filter applied to the texture.
     * @param horizontalWrapMode the type of wrapping applied to the texture
     *                           horizontally when it is sampled beyond its bounds.
     * @param verticalWrapMode   the type of wrapping applied to the texture
     *                           vertically when it is sampled beyond its bounds.
     * @return a new texture object which contains the image data
     * of the given texture resource.
     */
    static Texture get(final ResourceLocation location,
                       final TextureFilter minFilter,
                       final TextureFilter magFilter,
                       final TextureWrapMode horizontalWrapMode,
                       final TextureWrapMode verticalWrapMode) {
        return Peregrine.getTextureFactories().get(location,
            minFilter,
            magFilter,
            horizontalWrapMode,
            verticalWrapMode);
    }

    /**
     * Creates a new texture object described by the
     * given properties.
     *
     * @param location The location of the texture to
     *                 load into the newly allocated texture object.
     * @return a new texture object which contains the pixel
     * data of the given image resource.
     */
    static Texture get(final ResourceLocation location) {
        return Peregrine.getTextureFactories().get(location,
            TextureFilter.NEAREST,
            TextureFilter.NEAREST,
            TextureWrapMode.CLAMP,
            TextureWrapMode.CLAMP);
    }

    /**
     * Retrieves a proxy texture object for the given
     * OpenGL texture ID.
     *
     * @param textureId the OpenGL texture ID to create a
     *                  new texture object for.
     * @return a new texture object proxy that references
     * the given OpenGL texture.
     */
    static Texture get(final int textureId) {
        return Peregrine.getTextureFactories().get(textureId);
    }

    /**
     * Retrieves the format of this texture.
     *
     * @return the format of this texture.
     */
    TextureFormat getFormat();

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

    /**
     * Retrieves the width of the texture in pixels.
     *
     * @return the width of the texture in pixels.
     */
    int getWidth();

    /**
     * Retrieves the height of the texture in pixels.
     *
     * @return the height of the texture in pixels.
     */
    int getHeight();

    /**
     * Retrieves the minifying filter type applied
     * to this texture when sampling it.
     *
     * @return the minifying filter type of this texture.
     */
    TextureFilter getMinFilter();

    /**
     * Retrieves the magnifying filter type applied
     * to this texture when sampling it.
     *
     * @return the magnifying filter type of this texture.
     */
    TextureFilter getMagFilter();

    /**
     * Retrieves the horizontal texture wrap mode of this texture.
     *
     * @return the horizontal texture wrap mode of this texture.
     */
    TextureWrapMode getHorizontalWrapMode();

    /**
     * Retrieves the vertical texture wrap mode of this texture.
     *
     * @return the vertical texture wrap mode of this texture.
     */
    TextureWrapMode getVerticalWrapMode();

    @Override
    default int getAsInt() {
        return getId();
    }
}
