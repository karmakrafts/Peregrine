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
 * Represents a single sprite slot for a glyph
 * in a given font atlas texture.
 *
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface GlyphSprite {
    /**
     * Retrieves the metrics of this glyph sprite.
     *
     * @return the metrics of this glyph sprite.
     */
    GlyphMetrics getMetrics();

    /**
     * The side-size of this glyph sprite in the
     * font texture atlas in pixels.
     * Glyph sprites in the atlas are always square.
     *
     * @return the side-size of this glyph sprite in
     * the font texture atlas in pixels.
     */
    int getSize();

    /**
     * The U-offset of this sprite in the associated
     * font atlas texture.
     *
     * @return the U-offset of this sprite.
     */
    float getU();

    /**
     * The V-offset of this sprite in the associated
     * font atlas texture.
     *
     * @return the V-offset of this sprite.
     */
    float getV();
}
