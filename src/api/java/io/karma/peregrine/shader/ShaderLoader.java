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

package io.karma.peregrine.shader;

import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.nio.file.Path;

/**
 * Describes an interface for loading shaders on a per-object
 * or per-program basis. This allows implementing shader caching.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface ShaderLoader {
    /**
     * Called before the given shader program is being linked.
     *
     * @param program the shader program being loaded by this loader instance.
     */
    default void prepareProgram(final ShaderProgram program) {
    }


    /**
     * Called before the given shader program is linked in
     * order to allow loading the entire shader program from a file or resource.
     *
     * @param directory        the shader cache directory currently being used by the game.
     * @param resourceProvider the resource provider used for the current reload.
     * @param program          the shader program being loaded by this loader instance.
     * @return true if the given program doesn't need to be recompiled and linked.
     * false if the program needs to recompiled, linked and saved.
     */
    default boolean loadProgram(final Path directory,
                                final ResourceProvider resourceProvider,
                                final ShaderProgram program) {
        return false;
    }

    /**
     * Called after the given shader program has been linked
     * in order to allow saving the entire shader program to a file.
     *
     * @param directory        the shader cache directory currently being used by the game.
     * @param resourceProvider the resource provider used for the current reload.
     * @param program          the shader program being loaded by this loader instance.
     */
    default void saveProgram(final Path directory,
                             final ResourceProvider resourceProvider,
                             final ShaderProgram program) {
    }

    /**
     * Called after the given shader object has been compiled
     * in order to allow saving the shader object to a file.
     *
     * @param directory        the shader cache directory currently being used by the game.
     * @param resourceProvider the resource provider used for the current reload.
     * @param program          the shader program being loaded by this loader instance.
     * @param object           the shader object being loaded by this loader instance.
     */
    default void save(final Path directory,
                      final ResourceProvider resourceProvider,
                      final ShaderProgram program,
                      final ShaderObject object) {
    }

    /**
     * Called before the given shader object is being linked
     * in order to allow loading the shader object from a file or resource.
     *
     * @param directory        the shader cache directory currently being used by the game.
     * @param resourceProvider the resource provider used for the current reload.
     * @param program          the shader program being loaded by this loader instance.
     * @param object           the shader object being loaded by this loader instance.
     * @return a new {@link LoadResult} to indicate the required actions to
     * be performed on the shader object being loaded.
     */
    LoadResult load(final Path directory,
                    final ResourceProvider resourceProvider,
                    final ShaderProgram program,
                    final ShaderObject object);

    record LoadResult(boolean shouldCompile, boolean shouldSave) {
        public static final LoadResult COMPILE = new LoadResult(true, false);
        public static final LoadResult NONE = new LoadResult(false, false);
        public static final LoadResult COMPILE_AND_SAVE = new LoadResult(true, true);
    }
}
