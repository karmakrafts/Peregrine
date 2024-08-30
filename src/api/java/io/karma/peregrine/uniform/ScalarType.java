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

import io.karma.peregrine.Peregrine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BiFunction;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public enum ScalarType implements UniformType {
    // @formatter:off
    INT     (Integer.BYTES, 0,      Peregrine.getUniformTypeFactories().getInt()),
    LONG    (Long.BYTES,    0L,     Peregrine.getUniformTypeFactories().getLong()),
    FLOAT   (Float.BYTES,   0F,     Peregrine.getUniformTypeFactories().getFloat()),
    DOUBLE  (Double.BYTES,  0D,     Peregrine.getUniformTypeFactories().getDouble());
    // @formatter:on

    private final int size;
    private final Object defaultValue;
    private final BiFunction<String, Object, ? extends Uniform<?>> factory;

    ScalarType(final int size,
               final Object defaultValue,
               final BiFunction<String, Object, ? extends Uniform<?>> factory) {
        this.size = size;
        this.defaultValue = defaultValue;
        this.factory = factory;
    }

    @Override
    public boolean isSupported() {
        return switch (this) {
            case LONG -> Peregrine.supportsLongShaderType();
            case DOUBLE -> Peregrine.supportsDoubleShaderType();
            default -> true;
        };
    }

    @Override
    public int getHash() {
        return name().hashCode();
    }

    @Override
    public Uniform<?> create(final String name, final Object defaultValue) {
        return factory.apply(name, defaultValue);
    }

    @Override
    public int getComponentSize() {
        return size;
    }

    @Override
    public int getComponentCount() {
        return 1;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
