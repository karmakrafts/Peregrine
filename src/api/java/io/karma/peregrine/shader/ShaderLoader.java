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
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface ShaderLoader {
    default void prepareProgram(final ShaderProgram program) {
    }

    default boolean loadProgram(final Path directory,
                                final ResourceProvider resourceProvider,
                                final ShaderProgram program) {
        return false;
    }

    default void saveProgram(final Path directory,
                             final ResourceProvider resourceProvider,
                             final ShaderProgram program) {
    }

    default void save(final Path directory,
                      final ResourceProvider resourceProvider,
                      final ShaderProgram program,
                      final ShaderObject object) {
    }

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
