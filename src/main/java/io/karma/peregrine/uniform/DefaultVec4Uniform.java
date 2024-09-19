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

import io.karma.peregrine.api.shader.ShaderProgram;
import io.karma.peregrine.api.uniform.VectorUniform.Vec4Uniform;
import io.karma.peregrine.api.util.Requires;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultVec4Uniform extends AbstractUniform<Vector4f> implements Vec4Uniform {
    private final Vector4f value = new Vector4f();

    DefaultVec4Uniform(final String name, final Object defaultValue) {
        super(name);
        value.set(Requires.instanceOf(defaultValue, Vector4fc.class, "Default value is not a float vector"));
    }

    @Override
    public void apply(final ShaderProgram program) {
        if (!requiresUpdate) {
            return;
        }
        GL20.glUniform4f(program.getUniformLocation(name), value.x, value.y, value.z, value.w);
        requiresUpdate = false;
    }

    @Override
    public void upload(final long address) {
        if (!requiresUpdate) {
            return;
        }
        MemoryUtil.memPutFloat(address, value.x);
        MemoryUtil.memPutFloat(address + Float.BYTES, value.y);
        MemoryUtil.memPutFloat(address + (Float.BYTES << 1), value.z);
        MemoryUtil.memPutFloat(address + (Float.BYTES * 3), value.w);
        requiresUpdate = false;
    }

    @Override
    public void set(final Vector4f value) {
        if (this.value.equals(value)) {
            return;
        }
        this.value.set(value);
        requiresUpdate = true;
    }

    @Override
    public Vector4f get() {
        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        if(!(obj instanceof Vec4Uniform other)) {
            return false;
        }
        return value.equals(other.get());
    }

    @Override
    public String toString() {
        return String.format("DefaultVec4Uniform[name=%s,value=%s]", name, value);
    }
}
