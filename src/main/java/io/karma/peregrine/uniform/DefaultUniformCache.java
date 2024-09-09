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

import io.karma.peregrine.api.buffer.UniformBuffer;
import io.karma.peregrine.api.shader.ShaderProgram;
import io.karma.peregrine.api.uniform.Uniform;
import io.karma.peregrine.api.uniform.UniformCache;
import io.karma.peregrine.api.util.HashUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultUniformCache implements UniformCache {
    private final LinkedHashMap<String, ? extends Uniform<?>> uniforms;

    public DefaultUniformCache(final LinkedHashMap<String, ? extends Uniform<?>> uniforms) {
        this.uniforms = uniforms;
    }

    @Override
    public Map<String, ? extends Uniform<?>> getAll() {
        return uniforms;
    }

    @Override
    public void applyAll(final ShaderProgram program) {
        for (final var uniform : uniforms.values()) {
            uniform.apply(program);
        }
    }

    @Override
    public void uploadAll(final UniformBuffer buffer) {
        final var uniforms = this.uniforms.values();
        // Map UBO into memory so we can memcpy into it directly
        GL15.glBindBuffer(GL33.GL_UNIFORM_BUFFER, buffer.getId());
        final var address = GL15.nglMapBuffer(GL33.GL_UNIFORM_BUFFER, GL15.GL_WRITE_ONLY);
        for (final var uniform : uniforms) {
            // TODO: Replace name lookup with indices
            uniform.upload(address + buffer.getFieldOffset(uniform.getName()));
        }
        GL15.glUnmapBuffer(GL33.GL_UNIFORM_BUFFER);
        GL15.glBindBuffer(GL33.GL_UNIFORM_BUFFER, 0);
    }

    @Override
    public void updateAll() {
        for (final var uniform : uniforms.values()) {
            uniform.notifyUpdate();
        }
    }

    @Override
    public Uniform<?> get(final String name) {
        return Objects.requireNonNull(uniforms.get(name));
    }

    @Override
    public int hashCode() {
        return HashUtils.hash(uniforms);
    }
}
