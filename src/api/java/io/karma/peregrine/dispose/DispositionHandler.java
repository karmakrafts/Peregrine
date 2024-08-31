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

package io.karma.peregrine.dispose;

import java.util.List;

/**
 * Describes an interface which allows to register, unregister
 * and dispose objects which allocate native resources.
 * This may include but is not limited to shaders and textures.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
public interface DispositionHandler {
    /**
     * Register the given object to be disposed by this disposition handler.
     *
     * @param disposable The object to be registered.
     */
    void register(final Disposable disposable);

    /**
     * Unregister the given object to be no longer disposed by this disposition handler.
     *
     * @param disposable The object to be unregistered.
     */
    void unregister(final Disposable disposable);

    /**
     * Dispose all objects registered to this disposition handler instance
     * using the specified dispatchers.
     */
    void disposeAll();

    /**
     * Retrieves all objects registered with this disposition handler.
     *
     * @return all objects registered with this disposition handler.
     */
    List<Disposable> getObjects();
}
