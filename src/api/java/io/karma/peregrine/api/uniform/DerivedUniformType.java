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

import io.karma.peregrine.api.util.HashUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
final class DerivedUniformType implements UniformType {
    private final UniformType delegate;
    private final Object defaultValue;

    DerivedUniformType(final UniformType delegate, final Object defaultValue) {
        this.delegate = delegate;
        this.defaultValue = defaultValue;
    }

    @Override
    public int getComponentSize() {
        return delegate.getComponentSize();
    }

    @Override
    public int getComponentCount() {
        return delegate.getComponentCount();
    }

    @Override
    public Uniform<?> create(final String name, final Object defaultValue) {
        return delegate.create(name, defaultValue);
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public UniformType derive(final Object defaultValue) {
        return new DerivedUniformType(delegate, defaultValue);
    }

    @Override
    public int getHash() {
        return HashUtils.combine(delegate.getHash(), defaultValue.hashCode());
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DerivedUniformType other)) {
            return false;
        }
        return delegate.equals(other.delegate) && defaultValue.equals(other.defaultValue);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", delegate, defaultValue);
    }

    @Override
    public int hashCode() {
        return getHash();
    }
}
