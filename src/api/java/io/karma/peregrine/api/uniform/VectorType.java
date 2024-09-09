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

package io.karma.peregrine.api.uniform;

import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.api.util.HashUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;

import java.util.function.BiFunction;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public enum VectorType implements UniformType {
    // @formatter:off
    VEC2 (ScalarType.FLOAT,   2, new Vector2f(), Peregrine.getUniformTypeFactories().getVec2()),
    VEC3 (ScalarType.FLOAT,   3, new Vector2f(), Peregrine.getUniformTypeFactories().getVec3()),
    VEC4 (ScalarType.FLOAT,   4, new Vector2f(), Peregrine.getUniformTypeFactories().getVec4()),
    DVEC2(ScalarType.DOUBLE,  2, new Vector2f(), Peregrine.getUniformTypeFactories().getDVec2()),
    DVEC3(ScalarType.DOUBLE,  3, new Vector2f(), Peregrine.getUniformTypeFactories().getDVec3()),
    DVEC4(ScalarType.DOUBLE,  4, new Vector2f(), Peregrine.getUniformTypeFactories().getDVec4()),
    IVEC2(ScalarType.INT,     2, new Vector2f(), Peregrine.getUniformTypeFactories().getIVec2()),
    IVEC3(ScalarType.INT,     3, new Vector2f(), Peregrine.getUniformTypeFactories().getIVec3()),
    IVEC4(ScalarType.INT,     4, new Vector2f(), Peregrine.getUniformTypeFactories().getIVec4());
    // @formatter:on

    private final ScalarType type;
    private final int count;
    private final Object defaultValue;
    private final BiFunction<String, Object, ? extends Uniform<?>> factory;

    VectorType(final ScalarType type,
               final int count,
               final Object defaultValue,
               final BiFunction<String, Object, ? extends Uniform<?>> factory) {
        this.type = type;
        this.count = count;
        this.defaultValue = defaultValue;
        this.factory = factory;
    }

    @Override
    public boolean isSupported() {
        return type.isSupported();
    }

    @Override
    public int getHash() {
        return HashUtils.combine(type.getHash(), count);
    }

    @Override
    public Uniform<?> create(final String name, final Object defaultValue) {
        return factory.apply(name, defaultValue);
    }

    @Override
    public int getAlignment() {
        // @formatter:off
        return count == 3
            ? type.getComponentSize() << 1
            : type.getComponentSize() * count;
        // @formatter:on
    }

    @Override
    public int getComponentSize() {
        return type.getComponentSize();
    }

    @Override
    public int getComponentCount() {
        return count;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
