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

package io.karma.peregrine.api.reload;

import java.util.List;

/**
 * Describes an interface which allows to register and unregister
 * objects which may be reloaded by the games in-game reloading mechanism.
 * This may include but is not limited to shaders and textures.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
public interface ReloadHandler {
    /**
     * Register the given object to be reloaded by this reload handler.
     *
     * @param reloadable The object to be registered.
     */
    void register(final Reloadable reloadable);

    /**
     * Unregister the given object to be no longer reloaded by this reload handler.
     *
     * @param reloadable The object to be unregistered.
     */
    void unregister(final Reloadable reloadable);

    /**
     * Retrieves all objects registered with this reload handler.
     *
     * @return all objects registered with this reload handler.
     */
    List<Reloadable> getObjects();
}
