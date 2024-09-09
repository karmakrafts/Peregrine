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

package io.karma.peregrine.api.buffer;

import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.api.shader.ShaderProgram;
import io.karma.peregrine.api.uniform.UniformCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

/**
 * Represents a chunk of memory on the systems GPU
 * which contains variables passed to a shader program/pipeline.
 * This allows sharing uniforms as globals between different
 * shaders, without wasting additional memory and bus bandwidth.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface UniformBuffer extends Buffer {
    /**
     * Create a new uniform buffer described by the given properties.
     *
     * @param callback The callback which describes the uniform buffer to create.
     * @return A new uniform buffer with the given properties.
     */
    static UniformBuffer create(final Consumer<UniformBufferBuilder> callback) {
        return Peregrine.getUniformBufferFactory().apply(callback);
    }

    /**
     * Retrieves the internal uniform cache of this uniform buffer instance.
     *
     * @return the internal uniform cache of this uniform buffer instance.
     */
    UniformCache getCache();

    /**
     * The global binding point of this uniform buffer.
     * This value is unique for every uniform buffer thats created.
     *
     * @return The binding point of this uniform buffer.
     */
    int getBindingPoint();

    /**
     * Retrieves the offset into this buffers memory
     * for the given field in bytes.
     *
     * @param name The name of the field to retrieve the offset for.
     */
    int getFieldOffset(final String name);

    /**
     * Retrieves the size of this uniform buffer in bytes.
     *
     * @return The size of this uniform buffer in bytes.
     */
    int getSize();

    /**
     * Called after the linking stage for every shader program
     * which uses this uniform buffer.
     * Can be used to set up binding points and buffer ranges.
     *
     * @param name    The name of the uniform block to associate with this buffer.
     * @param program The shader program this uniform buffer is being used by.
     */
    void setup(final String name, final ShaderProgram program);

    /**
     * Called when the shader program using this uniform buffer
     * is being bound.
     * Can be used to update/upload all uniform values to the GPU memory.
     *
     * @param name    The name of the uniform block to associate with this buffer.
     * @param program The shader program this uniform buffer is being used by.
     */
    void bind(final String name, final ShaderProgram program);

    /**
     * Called when the shader program using this uniform buffer
     * is being unbound.
     * Can be used for resetting state.
     *
     * @param name    The name of the uniform block to associate with this buffer.
     * @param program The shader program this uniform buffer is being used by.
     */
    void unbind(final String name, final ShaderProgram program);
}
