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

/**
 * Describes a variant of a given font with a specified
 * size, style and variation axes.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
public interface FontVariant extends Font {
    /**
     * The default font variant size in points.
     */
    float DEFAULT_SIZE = 16F;

    /**
     * Retrieves the style of this font variant.
     *
     * @return the style of this font variant.
     */
    FontStyle getStyle();

    /**
     * Retrieves the size of this font variant in points.
     *
     * @return the size of this font variant in points.
     */
    float getSize();

    /**
     * Derives a new font variant from this variant
     * with the new given style.
     *
     * @param style the new style to derive from this font variant.
     * @return a font variant identical to this one with the new given style.
     */
    FontVariant withStyle(final FontStyle style);

    /**
     * Derives a new font variant from this variant
     * with the new given size in points.
     *
     * @param size the new style to derive from this font variant.
     * @return a font variant identical to this one with the new given size in points.
     */
    FontVariant withSize(final float size);

    /**
     * Derives a new font variant from this variant
     * with the new given variation axis.
     *
     * @param name  the name of the variation axis to specify.
     * @param value the value of the variation axis to specify.
     * @return a font variant identical to this one with the new given variation axis.
     */
    FontVariant withVar(final String name, final float value);

    /**
     * Derives a new font variant from this variant
     * with the new given style and size.
     *
     * @param style the new style to derive from this font variant.
     * @param size  the new size to derive from this font variant in points.
     * @return a font variant identical to this one with the new given style and size in points.
     */
    default FontVariant derive(final FontStyle style, final float size) {
        return withStyle(style).withSize(size);
    }

    /**
     * Computes a string which conforms to the pattern
     * specified by {@link net.minecraft.resources.ResourceLocation}
     * in order to be usable in file names/paths that
     * uniquely identifies this font variant.
     *
     * @return a string that uniquely identifies this font variant
     * and conforms to the pattern specified by {@link net.minecraft.resources.ResourceLocation}.
     */
    default String getVariantString() { // @formatter:off
        final var builder = new StringBuilder();
        final var path = getLocation().getPath();
        final var lastIndex = path.contains(".") ? path.lastIndexOf(".") : path.length() - 1;
        final var name = path.substring(path.lastIndexOf('/') + 1, lastIndex);
        builder.append(name)
            .append('_')
            .append(getStyle().name().toLowerCase())
            .append('_')
            .append(Float.toString(getSize()).replace('.', '_'));
        for(final var var : getVariationAxes().object2FloatEntrySet()) {
            builder.append('_')
                .append(var.getKey().toLowerCase())
                .append('_')
                .append(Float.toString(var.getFloatValue()).replace('.', '_'));
        }
        return builder.toString();
    } // @formatter:on
}
