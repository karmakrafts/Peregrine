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

package io.karma.peregrine.color;

import io.karma.peregrine.util.RectangleCorner;

/**
 * @author Alexander Hinze
 * @since 01/09/2024
 */
public final class Gradient implements ColorProvider {
    private final Color start;
    private final Color end;
    private final GradientSampler sampler;

    public Gradient(final Color start, final Color end, final GradientSampler sampler) {
        this.start = start;
        this.end = end;
        this.sampler = sampler;
    }

    public Color getStart() {
        return start;
    }

    public Color getEnd() {
        return end;
    }

    public GradientSampler getSampler() {
        return sampler;
    }

    @Override
    public int getColor(final RectangleCorner corner) {
        return sampler.sample(start.packARGB(), end.packARGB(), corner);
    }
}
