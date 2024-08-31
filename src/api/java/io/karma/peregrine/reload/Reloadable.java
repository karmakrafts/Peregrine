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

import io.karma.peregrine.util.Dispatcher;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.util.Comparator;

/**
 * Describes an object which may be reloaded through
 * the games in-game reload feature.
 * This process is split into two phases to allow
 * building object dependencies.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@FunctionalInterface
public interface Reloadable {
    /**
     * The default priority of reloadable objects.
     * This value is treated as neutral and won't cause
     * the associated reloadable object to be reordered
     * in the reload queue.
     */
    int DEFAULT_PRIORITY = 0;

    /**
     * A default comparator function which compares
     * the prepare priorities of the two given reloadable objects.
     */
    Comparator<Reloadable> PREP_COMPARATOR = Comparator.comparingInt(Reloadable::getPreparePriority);

    /**
     * A default comparator function which compares
     * the reload priorities of the two given reloadable objects.
     */
    Comparator<Reloadable> COMPARATOR = (a, b) -> Integer.compare(b.getReloadPriority(), a.getReloadPriority());

    /**
     * Determines whether this object will be reloaded on the
     * main (render/init) thread of the game, or on one of the
     * provided background threads created by the associated executor service.
     * <p>
     * If you wish to make any GL calls, leave this set to {@link Dispatcher#MAIN}.
     *
     * @return The dispatcher which invokes the reload
     * function of this reloadable object.
     */
    default Dispatcher getReloadDispatcher() {
        final var clazz = getClass();
        if (!clazz.isAnnotationPresent(ReloadPriority.class)) {
            return Dispatcher.MAIN;
        }
        return clazz.getAnnotation(ReloadPriority.class).dispatcher();
    }

    /**
     * Determines whether this object will be prepared on the
     * main (render/init) thread of the game, or on one of the
     * provided background threads created by the associated executor service.
     * <p>
     * If you wish to make any GL calls, leave this set to {@link Dispatcher#MAIN}.
     *
     * @return The dispatcher which invokes the prepare
     * function of this reloadable object.
     */
    default Dispatcher getPrepareDispatcher() {
        final var clazz = getClass();
        if (!clazz.isAnnotationPresent(ReloadPriority.class)) {
            return Dispatcher.MAIN;
        }
        return clazz.getAnnotation(ReloadPriority.class).dispatcher();
    }

    /**
     * Retrieves the priority at which this object should
     * be reloaded.
     *
     * @return The priority at which this object should be reloaded.
     */
    default int getReloadPriority() {
        final var clazz = getClass();
        if (!clazz.isAnnotationPresent(ReloadPriority.class)) {
            return DEFAULT_PRIORITY;
        }
        return clazz.getAnnotation(ReloadPriority.class).value();
    }

    /**
     * Retrieves the priority at which this object should
     * be prepared.
     *
     * @return The priority at which this object should be prepared.
     */
    default int getPreparePriority() {
        final var clazz = getClass();
        if (!clazz.isAnnotationPresent(PreparePriority.class)) {
            return getReloadPriority();
        }
        return clazz.getAnnotation(PreparePriority.class).value();
    }

    /**
     * Invoked to prepare this resource to be loaded/processed.
     * This can be used for updating internal state or invalidating objects.
     *
     * @param resourceProvider The resource provider used for this reload.
     */
    default void prepare(final ResourceProvider resourceProvider) {
    }

    /**
     * Invoked to reload this resource.
     * This can be used for streaming in resources from the JAR.
     *
     * @param resourceProvider The resource provider used for this reload.
     */
    void reload(final ResourceProvider resourceProvider);
}
