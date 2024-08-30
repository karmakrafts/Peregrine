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

package io.karma.peregrine.hooks;

import com.mojang.blaze3d.shaders.Program;
import io.karma.peregrine.shader.ShaderObject;
import io.karma.peregrine.shader.ShaderProgram;
import io.karma.peregrine.shader.ShaderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class PeregrineShaderAdaptor implements PeregrineShader {
    private final ShaderProgram delegate;
    private final HashMap<ShaderObject, PeregrineProgramAdaptor> adaptorCache = new HashMap<>();

    public PeregrineShaderAdaptor(final ShaderProgram delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getId() {
        return delegate.getId();
    }

    @Override
    public void apply() {
        delegate.bind();
    }

    @Override
    public void clear() {
        delegate.unbind();
    }

    @Override
    public void setSampler(final String name, final Object id) {
    }

    @Override
    public void markDirty() {
    }

    @Override
    public @NotNull Program getVertexProgram() {
        final var object = delegate.getObject(ShaderType.VERTEX);
        if (object instanceof Program) {
            return (Program) object;
        }
        return adaptorCache.computeIfAbsent(object, PeregrineProgramAdaptor::new);
    }

    @Override
    public @NotNull Program getFragmentProgram() {
        final var object = delegate.getObject(ShaderType.FRAGMENT);
        if (object instanceof Program) {
            return (Program) object;
        }
        return adaptorCache.computeIfAbsent(object, PeregrineProgramAdaptor::new);
    }

    @Override
    public void attachToProgram() {
    }
}
