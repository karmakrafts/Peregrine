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

package io.karma.peregrine.api.color;

import io.karma.peregrine.api.util.RectangleCorner;

/**
 * @author Alexander Hinze
 * @since 01/09/2024
 */
public enum DefaultGradientSampler implements GradientSampler {
    // @formatter:off
    HORIZONTAL    (DefaultGradientSampler::remapHorizontal),
    VERTICAL      (DefaultGradientSampler::remapVertical),
    ROTATED_45    (DefaultGradientSampler::remapRotated45),
    ROTATED_NEG_45(DefaultGradientSampler::remapRotatedNeg45);
    // @formatter:on

    private final GradientSampler function;

    DefaultGradientSampler(final GradientSampler function) {
        this.function = function;
    }

    private static int remapHorizontal(final int start, final int end, final RectangleCorner corner) {
        return switch (corner) {
            case TOP_LEFT, BOTTOM_LEFT -> start;
            default -> end;
        };
    }

    private static int remapVertical(final int start, final int end, final RectangleCorner corner) {
        return switch (corner) {
            case TOP_LEFT, TOP_RIGHT -> start;
            default -> end;
        };
    }

    private static int remapRotated45(final int start, final int end, final RectangleCorner corner) {
        return switch (corner) {
            case TOP_RIGHT, BOTTOM_LEFT -> start;
            default -> end;
        };
    }

    private static int remapRotatedNeg45(final int start, final int end, final RectangleCorner corner) {
        return switch (corner) {
            case TOP_LEFT, BOTTOM_RIGHT -> start;
            default -> end;
        };
    }

    @Override
    public int sample(final int start, final int end, final RectangleCorner corner) {
        return function.sample(start, end, corner);
    }
}
