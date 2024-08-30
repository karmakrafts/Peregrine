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
import io.karma.peregrine.uniform.ScalarUniform.LongUniform;
import io.karma.peregrine.util.HashUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.ARBGPUShaderInt64;
import org.lwjgl.system.MemoryUtil;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultLongUniform extends AbstractUniform<Long> implements LongUniform {
    private long value;

    DefaultLongUniform(final String name, final Object defaultValue) {
        super(name);
        if (!(defaultValue instanceof Number number)) {
            throw new IllegalArgumentException("Default value is not a number");
        }
        value = number.longValue();
    }

    @Override
    public long getLong() {
        return value;
    }

    public void setLong(final long value) {
        if (this.value == value) {
            return;
        }
        this.value = value;
        requiresUpdate = true;
    }

    @Override
    public UniformType getType() {
        return ScalarType.LONG;
    }

    @Override
    public void apply(final ShaderProgram program) {
        if (!requiresUpdate) {
            return;
        }
        ARBGPUShaderInt64.glUniform1i64ARB(program.getUniformLocation(name), value);
        requiresUpdate = false;
    }

    @Override
    public void upload(final long address) {
        if (!requiresUpdate) {
            return;
        }
        MemoryUtil.memPutLong(address, value);
        requiresUpdate = false;
    }

    @Override
    public int hashCode() {
        return HashUtils.combine(name.hashCode(), getType().getHash());
    }
}
