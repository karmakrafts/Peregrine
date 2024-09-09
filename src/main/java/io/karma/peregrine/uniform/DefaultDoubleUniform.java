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
import io.karma.peregrine.uniform.ScalarUniform.DoubleUniform;
import io.karma.peregrine.util.HashUtils;
import io.karma.peregrine.util.Requires;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.ARBGPUShaderFP64;
import org.lwjgl.system.MemoryUtil;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultDoubleUniform extends AbstractUniform<Double> implements DoubleUniform {
    private double value;

    DefaultDoubleUniform(final String name, final Object defaultValue) {
        super(name);
        value = Requires.instanceOf(defaultValue, Number.class, "Default value is not a number").doubleValue();
    }

    @Override
    public double getDouble() {
        return value;
    }

    public void setDouble(final double value) {
        if (this.value == value) {
            return;
        }
        this.value = value;
        requiresUpdate = true;
    }

    @Override
    public UniformType getType() {
        return ScalarType.DOUBLE;
    }

    @Override
    public void apply(final ShaderProgram program) {
        if (!requiresUpdate) {
            return;
        }
        ARBGPUShaderFP64.glUniform1d(program.getUniformLocation(name), value);
        requiresUpdate = false;
    }

    @Override
    public void upload(final long address) {
        if (!requiresUpdate) {
            return;
        }
        MemoryUtil.memPutDouble(address, value);
        requiresUpdate = false;
    }

    @Override
    public int hashCode() {
        return HashUtils.combine(name.hashCode(), getType().getHash());
    }
}
