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

package io.karma.peregrine.uniform;

import io.karma.peregrine.shader.ShaderProgram;
import io.karma.peregrine.uniform.VectorUniform.DVec2Uniform;
import io.karma.peregrine.util.Requires;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.lwjgl.opengl.ARBGPUShaderFP64;
import org.lwjgl.system.MemoryUtil;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultDVec2Uniform extends AbstractUniform<Vector2d> implements DVec2Uniform {
    private final Vector2d value = new Vector2d();

    DefaultDVec2Uniform(final String name, final Object defaultValue) {
        super(name);
        value.set(Requires.instanceOf(defaultValue, Vector2dc.class, "Default value is not a double vector"));
    }

    @Override
    public void apply(final ShaderProgram program) {
        if (!requiresUpdate) {
            return;
        }
        ARBGPUShaderFP64.glUniform2d(program.getUniformLocation(name), value.x, value.y);
        requiresUpdate = false;
    }

    @Override
    public void upload(final long address) {
        if (!requiresUpdate) {
            return;
        }
        MemoryUtil.memPutDouble(address, value.x);
        MemoryUtil.memPutDouble(address + Double.BYTES, value.y);
        requiresUpdate = false;
    }

    @Override
    public void set(final Vector2d value) {
        if (this.value.equals(value)) {
            return;
        }
        this.value.set(value);
        requiresUpdate = true;
    }

    @Override
    public Vector2d get() {
        return value;
    }
}
