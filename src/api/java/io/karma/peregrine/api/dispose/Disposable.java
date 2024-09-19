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

package io.karma.peregrine.api.dispose;

import io.karma.peregrine.api.util.Dispatcher;

import java.util.Comparator;

/**
 * Describes an object which allocates resources
 * that cannot be freed by the runtimes garbage collector.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@FunctionalInterface
public interface Disposable {
    /**
     * The default priority of disposable objects.
     * This value is treated as neutral and won't cause
     * the associated disposable object to be reordered
     * in the disposition queue.
     */
    int DEFAULT_DISPOSE_PRIORITY = 0;

    /**
     * A default comparator function which compares
     * the priorities of the two given disposable objects.
     */
    Comparator<? super Disposable> PRIORITY_COMPARATOR = (a, b) -> Integer.compare(b.getDisposePriority(),
        a.getDisposePriority());

    /**
     * Retrieves the priority at which this object should
     * be disposed.
     *
     * @return The priority at which this object should be disposed.
     */
    default int getDisposePriority() {
        final var clazz = getClass();
        if (!clazz.isAnnotationPresent(DisposePriority.class)) {
            return DEFAULT_DISPOSE_PRIORITY;
        }
        return clazz.getAnnotation(DisposePriority.class).value();
    }

    /**
     * Determines whether this object will be disposed on the
     * main (render/init) thread of the game, or on one of the
     * provided background threads created by the associated executor service.
     * <p>
     * If you wish to make any GL calls, leave this set to {@link Dispatcher#MAIN}.
     *
     * @return The dispatcher which invokes the dispose
     * function of this disposable object.
     */
    default Dispatcher getDisposeDispatcher() {
        final var clazz = getClass();
        if (!clazz.isAnnotationPresent(DisposePriority.class)) {
            return Dispatcher.MAIN;
        }
        return clazz.getAnnotation(DisposePriority.class).dispatcher();
    }

    /**
     * Disposes this object and it's underlying resources.
     */
    void dispose();
}
