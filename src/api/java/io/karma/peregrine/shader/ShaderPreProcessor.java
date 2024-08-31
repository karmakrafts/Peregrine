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

import java.util.function.Function;

/**
 * Describes a transformation which is applied to a given GLSL source
 * before compiling it into a {@link ShaderObject}.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface ShaderPreProcessor {
    /**
     * A pre-processor identity function which does
     * not modify the original GLSL source code.
     */
    ShaderPreProcessor PIPE = (source, prog, obj, loader) -> source;

    /**
     * Called by the given {@link ShaderObject} to pre-process
     * its GLSL source code before compiling it.
     * This allows implementing things like includes.
     *
     * @param source  the source code of the shader object to be compiled.
     * @param program the shader program the given shader object belongs to.
     * @param object  the shader object being pre-processed.
     * @param loader  a function for loading GLSL resources in the current reload context.
     * @return the processed GLSL source code to be compiled into the {@link ShaderObject}.
     */
    String process(final String source,
                   final ShaderProgram program,
                   final ShaderObject object,
                   final Function<ResourceLocation, String> loader);
}
