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
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface ShaderProgramBuilder {
    ShaderProgramBuilder loader(final Supplier<ShaderLoader> loaderSupplier);

    ShaderProgramBuilder format(final VertexFormat format);

    ShaderProgramBuilder shader(final Consumer<ShaderObjectBuilder> callback);

    ShaderProgramBuilder uniform(final String name, final UniformType type);

    ShaderProgramBuilder uniforms(final String name, final UniformBuffer buffer);

    ShaderProgramBuilder globalUniforms();

    ShaderProgramBuilder sampler(final String name);

    ShaderProgramBuilder sampler(final String name, final IntSupplier textureId);

    ShaderProgramBuilder sampler(final String name, final ResourceLocation location);

    ShaderProgramBuilder constant(final String name, final int value);

    ShaderProgramBuilder constant(final String name, final float value);

    ShaderProgramBuilder constant(final String name, final boolean value);

    ShaderProgramBuilder define(final String name);

    ShaderProgramBuilder define(final String name, final boolean value);

    ShaderProgramBuilder define(final String name, final int value);

    ShaderProgramBuilder define(final String name, final float value);

    ShaderProgramBuilder onBind(final Consumer<ShaderProgram> bindCallback);

    ShaderProgramBuilder onUnbind(final Consumer<ShaderProgram> unbindCallback);
}
