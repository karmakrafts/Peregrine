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

import com.mojang.blaze3d.vertex.VertexFormat;
import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.api.buffer.UniformBuffer;
import io.karma.peregrine.api.dispose.Disposable;
import io.karma.peregrine.api.reload.Reloadable;
import io.karma.peregrine.api.uniform.UniformCache;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Describes a full rendering pipeline
 * including the vertex buffer layout, shader objects (modules)
 * and uniform/storage buffer.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface ShaderProgram extends Disposable, Reloadable {
    int INVALID_ID = -1;

    /**
     * Creates a new shader program with the given properties.
     *
     * @param callback a callback invoked to describe the properties of the new shader program.
     * @return a new shader program with the given properties.
     */
    static ShaderProgram create(final Consumer<ShaderProgramBuilder> callback) {
        return Peregrine.getShaderProgramFactory().apply(callback);
    }

    /**
     * Retrieves the OpenGL ID of this shader program.
     * Take care when using directly as its easy to mess up state.
     *
     * @return the OpenGL ID of this shader program.
     */
    int getId();

    /**
     * Retrieves a list of all objects (modules) associated with
     * this shader program.
     *
     * @return a list of all objects (modules) associated with this
     * shader program.
     */
    List<ShaderObject> getObjects();

    /**
     * Retrieves the shader object (module) of the given type
     * associated with this shader program.
     *
     * @param type the type of the object to retrieve.
     * @return the shader object (module) of the given type.
     */
    ShaderObject getObject(final ShaderType type);

    /**
     * Retrieves the loader implementation used by
     * this shader program and its objects for streaming
     * in their sources/binaries.
     *
     * @return the loader implementation used by this
     * shader program and its objects for streaming in
     * their sources/binaries.
     */
    ShaderLoader getLoader();

    /**
     * Retrieves the vertex format used by this shader program.
     * This describes the layout of the data provided by VBO
     * rendered in the pass this program is being used in.
     *
     * @return the vertex format used by this shader program.
     */
    VertexFormat getVertexFormat();

    /**
     * Retrieves a {@link ShaderStateShard} which represents
     * this shader program or a proxy to it.
     *
     * @return a {@link ShaderStateShard} which represents this shader program.
     */
    ShaderStateShard asStateShard();

    /**
     * Makes this shader program the active one being
     * used in the current rendering pass.
     */
    void bind();

    /**
     * Makes this shader program no longer the active one
     * being used in the current rendering pass.
     */
    void unbind();

    /**
     * Retrieves the location of the given uniform
     * variable unique to this shader program.
     *
     * @param name the name of the uniform variable to query.
     * @return the location of the given variable unique to this shader program.
     */
    int getUniformLocation(final String name);

    /**
     * Retrieves the index of the given uniform block
     * unique to this shader program.
     *
     * @param name the name of the uniform block to query.
     * @return the index of the given uniform block unique to this shader program.
     */
    int getUniformBlockIndex(final String name);

    /**
     * Updates the given dynamic sampler with the given
     * OpenGL texture ID. The sampler must be specified
     * by a call to {@link ShaderProgramBuilder#sampler(String)}
     * when creating the shader program beforehand.
     *
     * @param name      the name of the sampler to update.
     * @param textureId the OpenGL ID of the texture to set for
     *                  the given dynamic sampler.
     */
    void setSampler(final String name, final int textureId);

    /**
     * Updates the given dynamic sampler with the texture
     * at the given location. The sampler must be specified
     * by a call to {@link ShaderProgramBuilder#sampler(String)}
     * when creating the shader program beforehand.
     *
     * @param name     the name of the sampler to update.
     * @param location the location of the texture to set for
     *                 the given dynamic sampler.
     */
    void setSampler(final String name, final ResourceLocation location);

    /**
     * Retrieves the sampler with the given name.
     *
     * @param name the name of the sampler to retrieve.
     * @return the sampler associated with the given name.
     */
    Sampler getSampler(final String name);

    /**
     * Determines whether this program is linked.
     *
     * @return true if this shader program is linked.
     */
    boolean isLinked();

    /**
     * Dynamically request a relink of this shader
     * program on the fly. Can be used for implementing
     * custom hot-reload behaviour.
     */
    void requestRelink();

    /**
     * Determines whether a relink is currently requested for
     * this shader program.
     *
     * @return true if a relink has been requested for this
     * shader program.
     */
    boolean isRelinkRequested();

    /**
     * Retrieves a map of all constants to be injected
     * into the GLSL source of all shader objects owned
     * by this shader program.
     *
     * @return a map of all constants to be injected
     * into the GLSL source of all shader objects owned
     * by this shader program.
     */
    Map<String, Object> getConstants();

    /**
     * Retrieves a map of all defines to be injected
     * into the GLSL source of all shader objects owned
     * by this shader program.
     *
     * @return a map of all defines to be injected
     * into the GLSL source of all shader objects owned
     * by this shader program.
     */
    Map<String, Object> getDefines();

    /**
     * Determines whether the given shader object is
     * currently attached to this shader program.
     *
     * @param object the object to be queried.
     * @return true if the given shader object is attached
     * to this shader program.
     */
    boolean isAttached(final ShaderObject object);

    /**
     * Retrieves the uniform cache used by this
     * shader program. Can be used for updating
     * uniform variables.
     *
     * @return the uniform cache used by this shader program.
     */
    UniformCache getUniforms();

    /**
     * Retrieves a map of all uniform buffers
     * used by this shader program.
     *
     * @return a map of all uniform buffers used by
     * this shader program.
     */
    Map<String, UniformBuffer> getUniformBuffers();
}
