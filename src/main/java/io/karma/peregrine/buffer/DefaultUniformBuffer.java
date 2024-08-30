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

package io.karma.peregrine.buffer;

import io.karma.peregrine.Peregrine;
import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.shader.ShaderProgram;
import io.karma.peregrine.uniform.DefaultUniformCache;
import io.karma.peregrine.uniform.Uniform;
import io.karma.peregrine.uniform.UniformCache;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.*;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultUniformBuffer implements UniformBuffer {
    private final UniformCache cache;
    private final BiConsumer<ShaderProgram, UniformBuffer> bindCallback;
    private final BiConsumer<ShaderProgram, UniformBuffer> unbindCallback;
    private final int size;
    private final Object2IntOpenHashMap<String> fieldOffsets = new Object2IntOpenHashMap<>();
    private final int bindingPoint;
    private int bufferId = -1;

    public DefaultUniformBuffer(final LinkedHashMap<String, ? extends Uniform<?>> uniforms,
                                final BiConsumer<ShaderProgram, UniformBuffer> bindCallback,
                                final BiConsumer<ShaderProgram, UniformBuffer> unbindCallback,
                                final int bindingPoint) {
        this.bindCallback = bindCallback;
        this.unbindCallback = unbindCallback;
        this.bindingPoint = bindingPoint;
        cache = new DefaultUniformCache(uniforms);
        size = cache.getAll().values().stream().mapToInt(u -> u.getType().getAlignedSize()).sum();

        // Compute all field offsets ahead of time
        var offset = 0;
        for (final var uniform : uniforms.entrySet()) {
            fieldOffsets.put(uniform.getKey(), offset);
            offset += uniform.getValue().getType().getAlignedSize();
        }

        PeregrineMod.DISPOSE_HANDLER.register(this);
    }

    @Override
    public int getId() {
        return bufferId;
    }

    @Override
    public int getBindingPoint() {
        return bindingPoint;
    }

    @Override
    public void setup(final String name, final ShaderProgram program) {
        if (bufferId == -1) {
            bufferId = GL15.glGenBuffers();
            GL15.glBindBuffer(GL33.GL_UNIFORM_BUFFER, bufferId);
            GL15.glBufferData(GL33.GL_UNIFORM_BUFFER, size, GL20.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL33.GL_UNIFORM_BUFFER, 0);
            Peregrine.LOGGER.debug("Created new uniform buffer object {} with {} bytes", bufferId, size);
        }
        final var blockIndex = program.getUniformBlockIndex(name);
        GL31.glUniformBlockBinding(program.getId(), blockIndex, bindingPoint);
        Peregrine.LOGGER.debug("Associated uniform block index {} with binding point {}", blockIndex, bindingPoint);
        GL30.glBindBufferBase(GL33.GL_UNIFORM_BUFFER, bindingPoint, bufferId);
    }

    @Override
    public void bind(final String name, final ShaderProgram program) {
        bindCallback.accept(program, this);
        cache.uploadAll(this);
    }

    @Override
    public void unbind(final String name, final ShaderProgram program) {
        unbindCallback.accept(program, this);
    }

    @Override
    public UniformCache getCache() {
        return cache;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getFieldOffset(final String name) {
        return fieldOffsets.getOrDefault(name, 0);
    }

    @Override
    public void dispose() {
        GL15.glDeleteBuffers(bufferId);
    }

    @Override
    public int hashCode() {
        return cache.hashCode();
    }

    @Override
    public String toString() {
        return String.format("DefaultUniformBuffer[id=%d,size=%d]", bufferId, size);
    }
}
