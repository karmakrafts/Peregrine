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

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
public interface ScalarUniform<T> extends Uniform<T> {
    interface DoubleUniform extends ScalarUniform<Double> {
        @Override
        default UniformType getType() {
            return ScalarType.DOUBLE;
        }

        double getDouble();

        void setDouble(final double value);

        @Override
        default void set(final Double value) {
            setDouble(value);
        }

        @Override
        default Double get() {
            return getDouble();
        }
    }

    interface FloatUniform extends ScalarUniform<Float> {
        @Override
        default UniformType getType() {
            return ScalarType.FLOAT;
        }

        float getFloat();

        void setFloat(final float value);

        @Override
        default void set(final Float value) {
            setFloat(value);
        }

        @Override
        default Float get() {
            return getFloat();
        }
    }

    interface IntUniform extends ScalarUniform<Integer> {
        @Override
        default UniformType getType() {
            return ScalarType.INT;
        }

        int getInt();

        void setInt(final int value);

        @Override
        default void set(final Integer value) {
            setInt(value);
        }

        @Override
        default Integer get() {
            return getInt();
        }
    }

    interface LongUniform extends ScalarUniform<Long> {
        @Override
        default UniformType getType() {
            return ScalarType.LONG;
        }

        long getLong();

        void setLong(final long value);

        @Override
        default void set(final Long value) {
            setLong(value);
        }

        @Override
        default Long get() {
            return getLong();
        }
    }
}
