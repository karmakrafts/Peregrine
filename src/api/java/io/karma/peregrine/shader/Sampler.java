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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Describes a sampler object for a given shader program.
 * This allows accessing textures in a shader.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface Sampler {
    /**
     * The per-shader-program ID of this sampler.
     * This is the value that is actually set as the uniform
     * entry for the sampler declaration in GLSL.
     *
     * @return the per-shader-program ID of this sampler.
     */
    int getId();

    /**
     * Retrieves the name of this sampler.
     * Must match the name of the uniform declaration in GLSL.
     *
     * @return the name of this sampler.
     */
    String getName();

    /**
     * Called after the link phase of the given shader program
     * to set up the internal state of this sampler instance.
     *
     * @param program the shader program this sampler is being used by.
     */
    void setup(final ShaderProgram program);

    /**
     * Called after the given shader program has been bound
     * to update/upload all uniform variables to GPU memory.
     *
     * @param program the shader program this sampler is being used by.
     */
    void bind(final ShaderProgram program);

    /**
     * Called before the given shader program has been unbound
     * to reset state.
     *
     * @param program the shader program this sampler is being used by.
     */
    void unbind(final ShaderProgram program);

    /**
     * Determines whether this sampler is dynamic or static.
     *
     * @return true if this sampler is dynamic.
     */
    default boolean isDynamic() {
        return true;
    }
}
