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
import io.karma.peregrine.api.uniform.MatrixUniform.DMat4Uniform;
import io.karma.peregrine.api.util.Requires;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.lwjgl.opengl.ARBGPUShaderFP64;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultDMat4Uniform extends AbstractUniform<Matrix4d> implements DMat4Uniform {
    private final Matrix4d value = new Matrix4d();

    DefaultDMat4Uniform(final String name, final Object defaultValue) {
        super(name);
        value.set(Requires.instanceOf(defaultValue, Matrix4dc.class, "Default matrix is not a double matrix"));
    }

    @Override
    public void apply(final ShaderProgram program) {
        if (!requiresUpdate) {
            return;
        }
        try (final var stack = MemoryStack.stackPush()) {
            ARBGPUShaderFP64.glUniformMatrix3dv(program.getUniformLocation(name),
                false,
                value.get(stack.mallocDouble(16)));
        }
        requiresUpdate = false;
    }

    @Override
    public void upload(final long address) {
        if (!requiresUpdate) {
            return;
        }
        try (final var stack = MemoryStack.stackPush()) {
            MemoryUtil.memCopy(MemoryUtil.memAddress(value.get(stack.mallocDouble(16))), address, Double.BYTES * 16);
        }
        requiresUpdate = false;
    }

    @Override
    public void set(final Matrix4d value) {
        if (this.value.equals(value)) {
            return;
        }
        this.value.set(value);
        requiresUpdate = true;
    }

    @Override
    public Matrix4d get() {
        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        if(!(obj instanceof DMat4Uniform other)) {
            return false;
        }
        return value.equals(other.get());
    }

    @Override
    public String toString() {
        return String.format("DefaultDMat4Uniform[name=%s,value=%s]", name, value);
    }
}
