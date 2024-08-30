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
import io.karma.peregrine.uniform.VectorUniform.IVec2Uniform;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultIVec2Uniform extends AbstractUniform<Vector2i> implements IVec2Uniform {
    private final Vector2i value = new Vector2i();

    DefaultIVec2Uniform(final String name, final Object defaultValue) {
        super(name);
        if (!(defaultValue instanceof Vector2ic vector)) {
            throw new IllegalArgumentException("Default value is not an integer");
        }
        value.set(vector);
    }

    @Override
    public void apply(final ShaderProgram program) {
        if (!requiresUpdate) {
            return;
        }
        GL20.glUniform2i(program.getUniformLocation(name), value.x, value.y);
        requiresUpdate = false;
    }

    @Override
    public void upload(final long address) {
        if (!requiresUpdate) {
            return;
        }
        MemoryUtil.memPutInt(address, value.x);
        MemoryUtil.memPutInt(address + Float.BYTES, value.y);
        requiresUpdate = false;
    }

    @Override
    public void set(final Vector2i value) {
        if (this.value.equals(value)) {
            return;
        }
        this.value.set(value);
        requiresUpdate = true;
    }

    @Override
    public Vector2i get() {
        return value;
    }
}
