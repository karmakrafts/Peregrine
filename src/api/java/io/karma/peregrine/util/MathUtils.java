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

package io.karma.peregrine.util;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
public final class MathUtils {
    public static final float EPSILON = 1E-6F;

    // @formatter:off
    private MathUtils() {}
    // @formatter:on

    public static boolean equals(final float a, final float b, final float epsilon) {
        return a >= b - epsilon && a <= b + epsilon;
    }
}
