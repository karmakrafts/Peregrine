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

import io.karma.peregrine.api.util.MemoryUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface UniformType {
    int getHash();

    Uniform<?> create(final String name, final Object defaultValue);

    default Uniform<?> create(final String name) {
        return create(name, getDefaultValue());
    }

    default UniformType derive(final Object defaultValue) {
        return new DerivedUniformType(this, defaultValue);
    }

    int getComponentSize();

    int getComponentCount();

    default int getAlignment() {
        return getComponentSize();
    }

    default int getSize() {
        return getComponentSize() * getComponentCount();
    }

    default int getAlignedSize() {
        return MemoryUtils.align(getSize(), getAlignment());
    }

    Object getDefaultValue();

    default boolean isSupported() {
        return true;
    }
}
