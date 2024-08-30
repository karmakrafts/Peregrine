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
import io.karma.peregrine.uniform.ScalarUniform.FloatUniform;
import io.karma.peregrine.util.HashUtils;
import io.karma.peregrine.util.MathUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultFloatUniform extends AbstractUniform<Float> implements FloatUniform {
    private float value;

    DefaultFloatUniform(final String name, final Object defaultValue) {
        super(name);
        if (!(defaultValue instanceof Number number)) {
            throw new IllegalArgumentException("Default value is not a float");
        }
        value = number.floatValue();
    }

    @Override
    public float getFloat() {
        return value;
    }

    @Override
    public void setFloat(final float value) {
        if (MathUtils.equals(this.value, value, MathUtils.EPSILON)) {
            return;
        }
        this.value = value;
        requiresUpdate = true;
    }

    @Override
    public UniformType getType() {
        return ScalarType.FLOAT;
    }

    @Override
    public void apply(final ShaderProgram program) {
        if (!requiresUpdate) {
            return;
        }
        GL20.glUniform1f(program.getUniformLocation(name), value);
        requiresUpdate = false;
    }

    @Override
    public void upload(final long address) {
        if (!requiresUpdate) {
            return;
        }
        MemoryUtil.memPutFloat(address, value);
        requiresUpdate = false;
    }

    @Override
    public int hashCode() {
        return HashUtils.combine(name.hashCode(), getType().getHash());
    }
}
