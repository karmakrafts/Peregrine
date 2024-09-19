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

import io.karma.peregrine.api.uniform.Uniform;
import io.karma.peregrine.api.util.HashUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public abstract class AbstractUniform<T> implements Uniform<T> {
    protected final String name;
    protected boolean requiresUpdate;

    protected AbstractUniform(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void notifyUpdate() {
        requiresUpdate = true;
    }

    @Override
    public boolean requiresUpdate() {
        return requiresUpdate;
    }

    @Override
    public int hashCode() {
        return HashUtils.combine(name.hashCode(), getType().getHash());
    }
}
