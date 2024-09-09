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

package io.karma.peregrine.api.texture;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Describes an interface for creating static
 * and dynamic texture objects which are automatically
 * managed by the Peregrine runtime.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface TextureFactories {
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
    DynamicTexture create(final TextureFilter minFilter,
                          final TextureFilter magFilter,
                          final TextureWrapMode horizontalWrapMode,
                          final TextureWrapMode verticalWrapMode);

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
    Texture get(final ResourceLocation location,
                final TextureFilter minFilter,
                final TextureFilter magFilter,
                final TextureWrapMode horizontalWrapMode,
                final TextureWrapMode verticalWrapMode);

    /**
     * Retrieves a proxy texture object for the given
     * OpenGL texture ID.
     *
     * @param textureId the OpenGL texture ID to create a
     *                  new texture object for.
     * @return a new texture object proxy that references
     * the given OpenGL texture.
     */
    Texture get(final int textureId);
}
