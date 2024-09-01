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

package io.karma.peregrine.buffer;

import io.karma.peregrine.shader.ShaderProgram;
import io.karma.peregrine.uniform.UniformType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Describes a builder pattern for creating new
 * immutable uniform buffer objects.
 * See {@link UniformBuffer#create(Consumer)}.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface UniformBufferBuilder {
    /**
     * Add a uniform with the given name to the newly created uniform buffer.
     *
     * @param name The name of the uniform to add. Must match the variable name in GLSL.
     * @param type The type of the uniform variable.
     * @return This builder instance.
     */
    UniformBufferBuilder uniform(final String name, final UniformType type);

    /**
     * Adds a callback to this uniform buffer which is invoked every time
     * the newly created buffer is being bound by a shader program.
     *
     * @param callback The callback to add to the newly created uniform buffer.
     * @return This builder instance.
     */
    UniformBufferBuilder onBind(final BiConsumer<ShaderProgram, UniformBuffer> callback);

    /**
     * Adds a callback to this uniform buffer which is invoked every time
     * the nenwly created buffer is being unbound by a shader program.
     *
     * @param callback The callback to add to the newly created uniform buffer.
     * @return This builder instance.
     */
    UniformBufferBuilder onUnbind(final BiConsumer<ShaderProgram, UniformBuffer> callback);
}
