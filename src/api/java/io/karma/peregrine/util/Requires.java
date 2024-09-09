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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

/**
 * @author Alexander Hinze
 * @since 09/09/2024
 */
@OnlyIn(Dist.CLIENT)
public final class Requires {
    // @formatter:off
    private Requires() {}
    // @formatter:on

    @SuppressWarnings("unchecked")
    public static <R> R instanceOf(final Object value, final Class<? extends R> type, final Supplier<String> messageGetter) {
        if (type.isInstance(value)) {
            return (R) value;
        }
        throw new IllegalStateException(messageGetter.get());
    }

    @SuppressWarnings("unchecked")
    public static <R> R instanceOf(final Object value, final Class<? extends R> type, final String message) {
        if (type.isInstance(value)) {
            return (R) value;
        }
        throw new IllegalStateException(message);
    }

    public static void that(final boolean condition, final Supplier<String> messageGetter) {
        if (!condition) {
            throw new IllegalStateException(messageGetter.get());
        }
    }

    public static void that(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
