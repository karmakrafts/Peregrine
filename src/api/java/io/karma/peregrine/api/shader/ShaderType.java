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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.ARBComputeShader;
import org.lwjgl.opengl.ARBTessellationShader;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;

import java.util.function.BooleanSupplier;

/**
 * Describes all types of shader objects (modules)
 * supported by Peregrine.
 *
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public enum ShaderType {
    // @formatter:off
    VERTEX   (GL20.GL_VERTEX_SHADER,                            () -> true),
    FRAGMENT (GL20.GL_FRAGMENT_SHADER,                          () -> true),
    TESS_CTRL(ARBTessellationShader.GL_TESS_CONTROL_SHADER,     () -> GL.getCapabilities().GL_ARB_tessellation_shader),
    TESS_EVAL(ARBTessellationShader.GL_TESS_EVALUATION_SHADER,  () -> GL.getCapabilities().GL_ARB_tessellation_shader),
    COMPUTE  (ARBComputeShader.GL_COMPUTE_SHADER,               () -> GL.getCapabilities().GL_ARB_compute_shader);
    // @formatter:on

    private final int glType;
    private final BooleanSupplier isSupported;

    ShaderType(final int glType, final BooleanSupplier isSupported) {
        this.glType = glType;
        this.isSupported = isSupported;
    }

    /**
     * Determines whether this shader type
     * is supported on the current platform.
     *
     * @return true if this shader type is supported.
     */
    public boolean isSupported() {
        return isSupported.getAsBoolean();
    }

    /**
     * Retrieves the internal OpenGL type
     * of this shader type.
     *
     * @return the internal OpenGL type of this shader type.
     */
    public int getGLType() {
        return glType;
    }
}
