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

package io.karma.peregrine.reload;

import io.karma.peregrine.Dispatcher;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Comparator;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@FunctionalInterface
public interface Reloadable {
    int DEFAULT_PRIORITY = 0;
    Comparator<Reloadable> PREP_COMPARATOR = Comparator.comparingInt(Reloadable::getPreparePriority);
    Comparator<Reloadable> COMPARATOR = (a, b) -> Integer.compare(b.getReloadPriority(), a.getReloadPriority());

    default Dispatcher getReloadDispatcher() {
        return Dispatcher.MAIN;
    }

    default Dispatcher getPrepareDispatcher() {
        return Dispatcher.MAIN;
    }

    default int getReloadPriority() {
        final var clazz = getClass();
        if (!clazz.isAnnotationPresent(ReloadPriority.class)) {
            return DEFAULT_PRIORITY;
        }
        return clazz.getAnnotation(ReloadPriority.class).value();
    }

    default int getPreparePriority() {
        final var clazz = getClass();
        if (!clazz.isAnnotationPresent(PreparePriority.class)) {
            return getReloadPriority();
        }
        return clazz.getAnnotation(PreparePriority.class).value();
    }

    default void prepare(final ResourceProvider resourceProvider) {
    }

    void reload(final ResourceProvider resourceProvider);
}
