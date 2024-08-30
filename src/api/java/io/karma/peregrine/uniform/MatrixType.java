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
import io.karma.peregrine.util.HashUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import java.util.function.BiFunction;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public enum MatrixType implements UniformType {
    // @formatter:off
    MAT2 (ScalarType.FLOAT,  2, new Matrix2f().identity(), Peregrine.getUniformTypeFactories().getMat2()),
    MAT3 (ScalarType.FLOAT,  3, new Matrix3f().identity(), Peregrine.getUniformTypeFactories().getMat3()),
    MAT4 (ScalarType.FLOAT,  4, new Matrix4f().identity(), Peregrine.getUniformTypeFactories().getMat4()),
    DMAT2(ScalarType.DOUBLE, 2, new Matrix2d().identity(), Peregrine.getUniformTypeFactories().getDMat2()),
    DMAT3(ScalarType.DOUBLE, 3, new Matrix3d().identity(), Peregrine.getUniformTypeFactories().getDMat3()),
    DMAT4(ScalarType.DOUBLE, 4, new Matrix4d().identity(), Peregrine.getUniformTypeFactories().getDMat4());
    // @formatter:on

    private final ScalarType type;
    private final int size;
    private final Object defaultValue;
    private final BiFunction<String, Object, ? extends MatrixUniform<?>> factory;

    MatrixType(final ScalarType type,
               final int size,
               final Object defaultValue,
               final BiFunction<String, Object, ? extends MatrixUniform<?>> factory) {
        this.type = type;
        this.size = size;
        this.defaultValue = defaultValue;
        this.factory = factory;
    }

    @Override
    public boolean isSupported() {
        return type.isSupported();
    }

    @Override
    public int getHash() {
        return HashUtils.combine(type.hashCode(), getComponentCount());
    }

    @Override
    public Uniform<?> create(final String name, final Object defaultValue) {
        return factory.apply(name, defaultValue);
    }

    @Override
    public int getComponentSize() {
        return type.getComponentSize();
    }

    @Override
    public int getComponentCount() {
        return size * size;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
