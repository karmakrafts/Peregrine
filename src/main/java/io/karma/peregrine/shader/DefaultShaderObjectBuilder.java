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

import com.google.common.base.Preconditions;
import io.karma.peregrine.PeregrineMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultShaderObjectBuilder implements ShaderObjectBuilder {
    private ShaderType type;
    private ResourceLocation location;
    private Supplier<ShaderPreProcessor> shaderPreProcessorSupplier = () -> PeregrineMod.SHADER_PRE_PROCESSOR;

    // @formatter:off
    DefaultShaderObjectBuilder() {}
    // @formatter:on

    @Override
    public ShaderObjectBuilder type(final ShaderType type) {
        this.type = type;
        return this;
    }

    @Override
    public ShaderObjectBuilder location(final ResourceLocation location) {
        this.location = location;
        return this;
    }

    @Override
    public ShaderObjectBuilder preProcessor(final Supplier<ShaderPreProcessor> shaderPreProcessorSupplier) {
        this.shaderPreProcessorSupplier = shaderPreProcessorSupplier;
        return this;
    }

    DefaultShaderObject build() {
        Preconditions.checkArgument(type != null, "Type of the shader must be specified");
        Preconditions.checkArgument(location != null, "Location of the shader must be specified");
        return new DefaultShaderObject(type, location, shaderPreProcessorSupplier);
    }
}
