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

import io.karma.peregrine.shader.ShaderProgram;
import io.karma.peregrine.uniform.Uniform;
import io.karma.peregrine.uniform.UniformType;
import io.karma.peregrine.util.Requires;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultUniformBufferBuilder implements UniformBufferBuilder {
    private static final BiConsumer<ShaderProgram, UniformBuffer> IDENTITY_CALLBACK = (s, b) -> {
    };
    private static int nextGlobalIndex;

    private final LinkedHashMap<String, Uniform<?>> uniforms = new LinkedHashMap<>();
    private BiConsumer<ShaderProgram, UniformBuffer> bindCallback = IDENTITY_CALLBACK;
    private BiConsumer<ShaderProgram, UniformBuffer> unbindCallback = IDENTITY_CALLBACK;

    public static UniformBuffer build(final Consumer<UniformBufferBuilder> callback) {
        final var builder = new DefaultUniformBufferBuilder();
        callback.accept(builder);
        return builder.build();
    }

    @Override
    public UniformBufferBuilder uniform(final String name, final UniformType type) {
        Requires.that(!uniforms.containsKey(name), () -> String.format("Uniform '%s' is already defined", name));
        uniforms.put(name, type.create(name));
        return this;
    }

    @Override
    public UniformBufferBuilder onBind(final BiConsumer<ShaderProgram, UniformBuffer> callback) {
        if (bindCallback == IDENTITY_CALLBACK) {
            bindCallback = callback;
            return this;
        }
        bindCallback = bindCallback.andThen(callback);
        return this;
    }

    @Override
    public UniformBufferBuilder onUnbind(final BiConsumer<ShaderProgram, UniformBuffer> callback) {
        if (unbindCallback == IDENTITY_CALLBACK) {
            unbindCallback = callback;
            return this;
        }
        unbindCallback = unbindCallback.andThen(callback);
        return this;
    }

    public DefaultUniformBuffer build() {
        return new DefaultUniformBuffer(uniforms, bindCallback, unbindCallback, nextGlobalIndex++);
    }
}
