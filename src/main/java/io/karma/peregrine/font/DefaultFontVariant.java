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

import io.karma.peregrine.api.font.*;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
public final class DefaultFontVariant implements FontVariant {
    private final Font font;
    private final FontStyle style;
    private final float size;
    private final Object2FloatOpenHashMap<String> variationAxisOverrides = new Object2FloatOpenHashMap<>();

    public DefaultFontVariant(final Font font, final FontStyle style, final float size) {
        this.font = font;
        this.style = style;
        this.size = size;
    }

    @Override
    public FontFamily getFamily() {
        return font.getFamily();
    }

    @Override
    public FontCharSet getSupportedChars() {
        return font.getSupportedChars();
    }

    @Override
    public ResourceLocation getLocation() {
        return font.getLocation();
    }

    @Override
    public FontVariant getDefaultVariant() {
        return font.getDefaultVariant();
    }

    @Override
    public FontStyle getStyle() {
        return style;
    }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public FontVariant withStyle(final FontStyle style) {
        return font.getFamily().getFont(style, size);
    }

    @Override
    public FontVariant withSize(final float size) {
        return font.getFamily().getFont(style, size);
    }

    @Override
    public FontVariant withVar(final String name, final float value) {
        final var variant = font.getFamily().getFont(style, size);
        if (!(variant instanceof DefaultFontVariant defaultVariant)) {
            return this;
        }
        defaultVariant.variationAxisOverrides.put(name, value);
        return variant;
    }

    @Override
    public float getVariationAxis(final String name) {
        return variationAxisOverrides.getOrDefault(name, font.getVariationAxis(name));
    }

    @Override
    public Object2FloatMap<String> getVariationAxes() {
        final var axes = new Object2FloatOpenHashMap<>(font.getVariationAxes());
        axes.putAll(variationAxisOverrides);
        return axes;
    }
}