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

import io.karma.peregrine.api.texture.*;
import io.karma.peregrine.api.util.HashUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultTextureFactories implements TextureFactories {
    private final Int2ObjectOpenHashMap<DefaultTexture> staticTextures = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<ProxyTexture> proxyTextures = new Int2ObjectOpenHashMap<>();

    @Override
    public DynamicTexture create(final TextureFormat format,
                                 final TextureFilter minFilter,
                                 final TextureFilter magFilter,
                                 final TextureWrapMode horizontalWrapMode,
                                 final TextureWrapMode verticalWrapMode) {
        return new DefaultDynamicTexture(format, minFilter, magFilter, horizontalWrapMode, verticalWrapMode);
    }

    @Override
    public Texture get(final ResourceLocation location,
                       final TextureFilter minFilter,
                       final TextureFilter magFilter,
                       final TextureWrapMode horizontalWrapMode,
                       final TextureWrapMode verticalWrapMode) {
        return staticTextures.computeIfAbsent(HashUtils.combineMany(location.hashCode(),
                minFilter.ordinal(),
                magFilter.ordinal(),
                horizontalWrapMode.ordinal(),
                verticalWrapMode.ordinal()),
            hash -> new DefaultTexture(location, minFilter, magFilter, horizontalWrapMode, verticalWrapMode));
    }

    @Override
    public Texture get(final int textureId) {
        return proxyTextures.computeIfAbsent(textureId, ProxyTexture::new);
    }
}
