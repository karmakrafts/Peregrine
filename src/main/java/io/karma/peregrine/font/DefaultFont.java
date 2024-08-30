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

package io.karma.peregrine.font;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
public final class DefaultFont implements Font {
    private final FontFamily family;
    private final FontCharSet supportedChars;
    private final ResourceLocation location;
    private final DefaultFontVariant defaultVariant;
    private final Object2FloatOpenHashMap<String> variationAxes = new Object2FloatOpenHashMap<>();

    public DefaultFont(final FontFamily family, final FontCharSet supportedChars, final ResourceLocation location) {
        this.family = family;
        this.supportedChars = supportedChars;
        this.location = location;
        defaultVariant = new DefaultFontVariant(this, FontStyle.REGULAR, FontVariant.DEFAULT_SIZE);
    }

    @Override
    public Object2FloatMap<String> getVariationAxes() {
        return Object2FloatMaps.unmodifiable(variationAxes);
    }

    public void setVariationAxes(final Object2FloatMap<String> variationAxes) {
        this.variationAxes.putAll(variationAxes);
    }

    @Override
    public float getVariationAxis(final String name) {
        return variationAxes.getOrDefault(name, 0F);
    }

    @Override
    public FontVariant getDefaultVariant() {
        return defaultVariant;
    }

    @Override
    public FontCharSet getSupportedChars() {
        return supportedChars;
    }

    @Override
    public FontFamily getFamily() {
        return family;
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }
}