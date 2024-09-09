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

import io.karma.peregrine.api.font.GlyphMetrics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultGlyphMetrics implements GlyphMetrics {
    private final float width;
    private final float height;
    private final float ascent;
    private final float descent;
    private final float advanceX;
    private final float advanceY;
    private final float bearingX;
    private final float bearingY;

    public DefaultGlyphMetrics(final float width,
                               final float height,
                               final float ascent,
                               final float descent,
                               final float advanceX,
                               final float advanceY,
                               final float bearingX,
                               final float bearingY) {
        this.width = width;
        this.height = height;
        this.ascent = ascent;
        this.descent = descent;
        this.advanceX = advanceX;
        this.advanceY = advanceY;
        this.bearingX = bearingX;
        this.bearingY = bearingY;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public float getAscent() {
        return ascent;
    }

    @Override
    public float getDescent() {
        return descent;
    }

    @Override
    public float getAdvanceX() {
        return advanceX;
    }

    @Override
    public float getAdvanceY() {
        return advanceY;
    }

    @Override
    public float getBearingX() {
        return bearingX;
    }

    @Override
    public float getBearingY() {
        return bearingY;
    }

    @Override
    public String toString() {
        return String.format("[ASC%f,DES%f,ADV%f,BX%f,BY%f]", ascent, descent, advanceX, bearingX, bearingY);
    }
}
