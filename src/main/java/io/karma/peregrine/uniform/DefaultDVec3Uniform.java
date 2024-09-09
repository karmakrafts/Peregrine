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
import io.karma.peregrine.uniform.VectorUniform.DVec3Uniform;
import io.karma.peregrine.util.Requires;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.lwjgl.opengl.ARBGPUShaderFP64;
import org.lwjgl.system.MemoryUtil;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultDVec3Uniform extends AbstractUniform<Vector3d> implements DVec3Uniform {
    private final Vector3d value = new Vector3d();

    DefaultDVec3Uniform(final String name, final Object defaultValue) {
        super(name);
        value.set(Requires.instanceOf(defaultValue, Vector3dc.class, "Default value is not a double vector"));
    }

    @Override
    public void apply(final ShaderProgram program) {
        if (!requiresUpdate) {
            return;
        }
        ARBGPUShaderFP64.glUniform3d(program.getUniformLocation(name), value.x, value.y, value.z);
        requiresUpdate = false;
    }

    @Override
    public void upload(final long address) {
        if (!requiresUpdate) {
            return;
        }
        MemoryUtil.memPutDouble(address, value.x);
        MemoryUtil.memPutDouble(address + Double.BYTES, value.y);
        MemoryUtil.memPutDouble(address + (Double.BYTES << 1), value.z);
        requiresUpdate = false;
    }

    @Override
    public void set(final Vector3d value) {
        if (this.value.equals(value)) {
            return;
        }
        this.value.set(value);
        requiresUpdate = true;
    }

    @Override
    public Vector3d get() {
        return value;
    }
}
