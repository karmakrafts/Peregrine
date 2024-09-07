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

import com.mojang.blaze3d.vertex.VertexFormat;
import io.karma.peregrine.buffer.UniformBuffer;
import io.karma.peregrine.uniform.UniformType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Describes a builder pattern for creating new
 * immutable shader programs.
 * See {@link ShaderProgram#create(Consumer)}.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface ShaderProgramBuilder {
    /**
     * Specifies the loader implementation used by the
     * newly created shader program's shader objects to
     * load their GLSL source.
     *
     * @param loaderSupplier a supplier which returns the
     *                       loader implementation used by the
     *                       shader objects owned by the newly
     *                       created shader program.
     * @return this builder instance.
     */
    ShaderProgramBuilder loader(final Supplier<ShaderLoader> loaderSupplier);

    /**
     * Specifies the vertex format used by the
     * newly created shader program.
     *
     * @param format the vertex format used by
     *               the newly created shader program.
     * @return this builder instance.
     */
    ShaderProgramBuilder format(final VertexFormat format);

    /**
     * Specifies a new shader object (module)
     * owned by the newly created shader program.
     *
     * @param callback a callback for describing the properties
     *                 of the newly created shader object.
     * @return this builder instance.
     */
    ShaderProgramBuilder stage(final Consumer<ShaderObjectBuilder> callback);

    /**
     * Specifies a uniform variable made available
     * to the objects (modules) owned by the newly
     * created shader program.
     *
     * @param name the name of the uniform variable.
     * @param type the type of the uniform variable.
     *             See {@link io.karma.peregrine.uniform.ScalarType},
     *             {@link io.karma.peregrine.uniform.VectorType}
     *             and {@link io.karma.peregrine.uniform.MatrixType}.
     * @return this builder instance.
     */
    ShaderProgramBuilder uniform(final String name, final UniformType type);

    /**
     * Specifies a uniform block made available
     * to the objects (modules) owned by the newly
     * created shader program.
     *
     * @param name   the name of the uniform block.
     * @param buffer the buffer used to store the values
     *               of the uniform block.
     * @return this builder instance.
     */
    ShaderProgramBuilder uniforms(final String name, final UniformBuffer buffer);

    /**
     * Specifies a uniform block called {@code Globals}
     * which contains the default vanilla uniforms
     * ({@code ProjMat}, {@code ModelViewMat}, {@code ColorModulator} and {@code Time})
     * which is made available to the objects (modules)
     * owned by the newly created shader program.
     *
     * @return this builder instance.
     */
    ShaderProgramBuilder globalUniforms();

    /**
     * Specifies a new dynamic sampler made available
     * to the fragment (pixel) stage of the newly created
     * shader program.
     *
     * @param name the name of the sampler uniform variable.
     * @return this builder instance.
     */
    ShaderProgramBuilder sampler(final String name);

    /**
     * Specifies a new static sampler made available
     * to the fragment (pixel) stage of the newly created
     * shader program.
     *
     * @param name      the name of the sampler uniform variable.
     * @param textureId a supplier function which returns a
     *                  valid OpenGL texture ID to be bound to
     *                  the newly created sampler.
     * @return this builder instance.
     */
    ShaderProgramBuilder sampler(final String name, final IntSupplier textureId);

    /**
     * Specifies a new static sampler made available
     * to the fragment (pixel) stage of the newly created
     * shader program.
     *
     * @param name     the name of the sampler uniform variable.
     * @param location the location of the texture to associate
     *                 with the newly created sampler.
     * @return this builder instance.
     */
    ShaderProgramBuilder sampler(final String name, final ResourceLocation location);

    /**
     * Specifies an int specialization constant in the GLSL source
     * of the objects owned by the newly created shader program.
     *
     * @param name  the name of the specialization constant.
     * @param value the value of the specialization constant.
     * @return this builder instance.
     */
    ShaderProgramBuilder constant(final String name, final int value);

    /**
     * Specifies a float specialization constant in the GLSL source
     * of the objects owned by the newly created shader program.
     *
     * @param name  the name of the specialization constant.
     * @param value the value of the specialization constant.
     * @return this builder instance.
     */
    ShaderProgramBuilder constant(final String name, final float value);

    /**
     * Specifies a boolean specialization constant in the GLSL source
     * of the objects owned by the newly created shader program.
     *
     * @param name  the name of the specialization constant.
     * @param value the value of the specialization constant.
     * @return this builder instance.
     */
    ShaderProgramBuilder constant(final String name, final boolean value);

    /**
     * Specifies a pre-processor constant in the GLSL source
     * of the objects owned by the newly created shader program.
     * The value of the pre-processor constant will be set to 1.
     *
     * @param name the name of the pre-processor constant.
     * @return this builder instance.
     */
    ShaderProgramBuilder define(final String name);

    /**
     * Specifies a boolean pre-processor constant in the GLSL source
     * of the objects owned by the newly created shader program.
     *
     * @param name  the name of the pre-processor constant.
     * @param value the value of the pre-processor constant.
     * @return this builder instance.
     */
    ShaderProgramBuilder define(final String name, final boolean value);

    /**
     * Specifies an int pre-processor constant in the GLSL source
     * of the objects owned by the newly created shader program.
     *
     * @param name  the name of the pre-processor constant.
     * @param value the value of the pre-processor constant.
     * @return this builder instance.
     */
    ShaderProgramBuilder define(final String name, final int value);

    /**
     * Specifies a float pre-processor constant in the GLSL source
     * of the objects owned by the newly created shader program.
     *
     * @param name  the name of the pre-processor constant.
     * @param value the value of the pre-processor constant.
     * @return this builder instance.
     */
    ShaderProgramBuilder define(final String name, final float value);

    /**
     * Adds a callback to the newly created shader program which
     * is invoked when the program is being bound.
     *
     * @param bindCallback the callback to invoke when the newly
     *                     created shader program is being bound.
     * @return this builder instance.
     */
    ShaderProgramBuilder onBind(final Consumer<ShaderProgram> bindCallback);

    /**
     * Adds a callback to the newly created shader program which
     * is invoked when the program is being unbound.
     *
     * @param unbindCallback the callback to invoke when the newly
     *                       created shader program is being unbound.
     * @return this builder instance.
     */
    ShaderProgramBuilder onUnbind(final Consumer<ShaderProgram> unbindCallback);
}
