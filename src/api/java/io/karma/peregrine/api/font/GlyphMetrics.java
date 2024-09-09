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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Describes the dimensions and parameters of a single glyph.
 * See <a href="https://freetype.org/freetype2/docs/glyphs/glyphs-3.html" target="_blank">the examples in the FreeType documentation</a>.
 *
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface GlyphMetrics {
    /**
     * Retrieves the width of the associated glyph in partial pixels.
     *
     * @return the width of the associated glyph in partial pixels.
     */
    float getWidth();

    /**
     * Retrieves the height of the associated glyph in partial pixels.
     *
     * @return the height of the associated glyph in partial pixels.
     */
    float getHeight();

    /**
     * Retrieves the ascent of the associated glyph in partial pixels.
     *
     * @return the ascent of the associated glyph in partial pixels.
     */
    float getAscent();

    /**
     * Retrieves the descent of the associated glyph in partial pixels.
     *
     * @return the descent of the associated glyph in partial pixels.
     */
    float getDescent();

    /**
     * Retrieves the amount of partial pixels which the current y-position
     * needs to be incremented after rendering the associated glyph.
     *
     * @return the amount of partial pixels to increment after
     * rendering the associated glyph.
     */
    float getAdvanceX();

    /**
     * Retrieves the amount of partial pixels which the current x-position
     * needs to be incremented after rendering the associated glyph.
     *
     * @return the amount of partial pixels to increment after
     * rendering the associated glyph.
     */
    float getAdvanceY();

    /**
     * Retrieves the amount of partial pixels from the current pen position
     * to the left bbox edge of the next glyph.
     *
     * @return the amount of partial pixels from the current pen position
     * to the left bbox edge of the next glyph.
     */
    float getBearingX();

    /**
     * Retrieves the amount of partial pixels from the current pen position
     * to the upper bbox edge of the next glyph.
     *
     * @return the amount of partial pixels from the current pen position
     * to the upper bbox edge of the next glyph.
     */
    float getBearingY();
}
