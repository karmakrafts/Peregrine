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

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

/**
 * Describes a builder pattern for creating
 * new immutable shader objects.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface ShaderObjectBuilder {
    /**
     * Specifies the type of the newly created {@link ShaderObject}.
     *
     * @param type the type of the newly created {@link ShaderObject}.
     * @return this builder instance.
     */
    ShaderObjectBuilder type(final ShaderType type);

    /**
     * Specifies the location of the GLSL source used by
     * the newly created shader object.
     *
     * @param location the location of the GLSL source used
     *                 by the newly created {@link ShaderObject}.
     * @return this builder instance.
     */
    ShaderObjectBuilder location(final ResourceLocation location);

    /**
     * Specifies the location of the GLSL source used by
     * the newly created {@link ShaderObject}.
     *
     * @param modId the mod ID which the GLSL file resource belongs to.
     * @param path  the path to the GLSL resource.
     * @return this builder instance.
     */
    default ShaderObjectBuilder location(final String modId, final String path) {
        return location(new ResourceLocation(modId, path));
    }

    /**
     * Specifies the pre-processor used for processing
     * the GLSL source of the newly created {@link ShaderObject}.
     *
     * @param shaderPreProcessorSupplier a supplier which returns an
     *                                   instance of {@link ShaderPreProcessor} used for processing the GLSL
     *                                   source of the newly created {@link ShaderObject}.
     * @return this builder instance.
     */
    ShaderObjectBuilder preProcessor(final Supplier<ShaderPreProcessor> shaderPreProcessorSupplier);
}
