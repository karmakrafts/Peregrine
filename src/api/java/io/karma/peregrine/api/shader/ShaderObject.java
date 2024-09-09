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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.nio.file.Path;

/**
 * Describes a singular shader pipeline stage (or module)
 * for a given shader program.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface ShaderObject {
    /**
     * Retrieves the location of the GLSL source file
     * of this shader object.
     *
     * @return the location of the GLSL source file of this shader object.
     */
    ResourceLocation getLocation();

    /**
     * Retrieves the type of this shader object.
     *
     * @return the type of this shader object.
     */
    ShaderType getType();

    /**
     * Retrieves the OpenGL ID of this shader object.
     * Take care when using directly as its easy to mess up state.
     *
     * @return the OpenGL ID of this shader object.
     */
    int getId();

    /**
     * Determines whether this shader object is compiled.
     *
     * @return true if this shader object is compiled.
     */
    boolean isCompiled();

    /**
     * Recompiles this shader object.
     * The shader object may become unusable while it is recompiling.
     *
     * @param directory        the cache directory currently being used by the game.
     * @param program          the shader program this shader object belongs to.
     * @param resourceProvider the resource provider being used for the current reload.
     * @return true if the shader program should be linked when all
     * objects have been recompiled. false if an error occurred while recompiling.
     */
    boolean recompile(final Path directory, final ShaderProgram program, final ResourceProvider resourceProvider);

    /**
     * Attach this shader object to the given shader program.
     *
     * @param program the shader program to attach this object to.
     */
    void attach(final ShaderProgram program);

    /**
     * Detach this shader object from the given shader program.
     *
     * @param program the shader program to detach this object from.
     */
    void detach(final ShaderProgram program);

    /**
     * Retrieves the pre-processor used for processing
     * the GLSL source of this shader object.
     *
     * @return the pre-processor used for processing
     * the GLSL source of this shader object.
     */
    ShaderPreProcessor getPreProcessor();
}
