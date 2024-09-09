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
import io.karma.peregrine.api.font.GlyphSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultGlyphSprite implements GlyphSprite {
    private final GlyphMetrics metrics;
    private final int size;
    private final float u;
    private final float v;

    public DefaultGlyphSprite(final GlyphMetrics metrics, final int size, final float u, final float v) {
        this.metrics = metrics;
        this.size = size;
        this.u = u;
        this.v = v;
    }

    @Override
    public GlyphMetrics getMetrics() {
        return metrics;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public float getU() {
        return u;
    }

    @Override
    public float getV() {
        return v;
    }
}
