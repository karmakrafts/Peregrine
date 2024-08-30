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
 * @author Alexander Hinze
 * @since 30/08/2024
 */
public interface FontVariant extends Font {
    float DEFAULT_SIZE = 16F;

    FontStyle getStyle();

    float getSize();

    FontVariant withStyle(final FontStyle style);

    FontVariant withSize(final float size);

    FontVariant withVar(final String name, final float value);

    default FontVariant derive(final FontStyle style, final float size) {
        return withStyle(style).withSize(size);
    }

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
