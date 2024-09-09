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
import io.karma.peregrine.uniform.VectorUniform.Vec2Uniform;
import io.karma.peregrine.util.Requires;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultVec2Uniform extends AbstractUniform<Vector2f> implements Vec2Uniform {
    private final Vector2f value = new Vector2f();

    DefaultVec2Uniform(final String name, final Object defaultValue) {
        super(name);
        value.set(Requires.instanceOf(defaultValue, Vector2fc.class, "Default value is not a float vector"));
    }

    @Override
    public void apply(final ShaderProgram program) {
        if (!requiresUpdate) {
            return;
        }
        GL20.glUniform2f(program.getUniformLocation(name), value.x, value.y);
        requiresUpdate = false;
    }

    @Override
    public void upload(final long address) {
        if (!requiresUpdate) {
            return;
        }
        MemoryUtil.memPutFloat(address, value.x);
        MemoryUtil.memPutFloat(address + Float.BYTES, value.y);
        requiresUpdate = false;
    }

    @Override
    public void set(final Vector2f value) {
        if (this.value.equals(value)) {
            return;
        }
        this.value.set(value);
        requiresUpdate = true;
    }

    @Override
    public Vector2f get() {
        return value;
    }
}
