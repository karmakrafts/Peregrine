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

package io.karma.peregrine.api.font;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.resources.ResourceLocation;

/**
 * Describes a single font from a given font family.
 * This includes the font file location and possible variation axes.
 * Also provides a function for retrieving the default variant of this font.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
public interface Font {
    /**
     * Retrieves the family this font belongs to.
     *
     * @return the family this font belongs to.
     */
    FontFamily getFamily();

    /**
     * Retrieves a set of all characters supported by this font.
     *
     * @return a set of all characters supported by this font.
     */
    FontCharSet getSupportedChars();

    /**
     * Retrieves the location of the font file this font originates from.
     *
     * @return the location of the font file this font originates from.
     */
    ResourceLocation getLocation();

    /**
     * Retrieves the default variant of this font.
     *
     * @return the default variant of this font.
     */
    FontVariant getDefaultVariant();

    /**
     * Retrieves the variation axes supported by this font.
     *
     * @return the variation axes supported by this font.
     */
    Object2FloatMap<String> getVariationAxes();

    /**
     * Retrieves the value of the given variation axis
     * associated with this font.
     *
     * @param name The name of the variation axis. Specified by the font itself.
     * @return The value of the given variation axis.
     */
    float getVariationAxis(final String name);

    /**
     * Reinterpret this font as a font variant.
     * If this font instance is already a variant, cast it, otherwise
     * retrieve this fonts default variant.
     *
     * @return this font as a variant.
     */
    default FontVariant asVariant() {
        return this instanceof FontVariant variant ? variant : getDefaultVariant();
    }
}
