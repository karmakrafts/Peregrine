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

import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
public final class DI {
    private final HashMap<Class<?>, Object> instances = new HashMap<>();

    public DI() {}

    public <T, U extends T> void put(final Class<T> type, final U instance) {
        instances.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(final Class<T> type) {
        return (T) instances.get(type);
    }
}
