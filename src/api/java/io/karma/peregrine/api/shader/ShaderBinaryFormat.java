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

package io.karma.peregrine.api.shader;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Represents a binary shader format supported
 * by the current OpenGL implementation.
 * This wrapper type is used in the initialization {@link io.karma.peregrine.api.util.DI}.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public record ShaderBinaryFormat(int value) {
    private static final int INVALID_VALUE = -1;
    public static final ShaderBinaryFormat NONE = new ShaderBinaryFormat(INVALID_VALUE);

    public boolean isValid() {
        return value != INVALID_VALUE;
    }
}
