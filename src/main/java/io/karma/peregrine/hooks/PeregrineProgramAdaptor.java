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
import com.mojang.blaze3d.shaders.Shader;
import io.karma.peregrine.api.shader.ShaderObject;
import io.karma.peregrine.api.shader.ShaderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class PeregrineProgramAdaptor extends Program {
    private final ShaderObject delegate;

    public PeregrineProgramAdaptor(final ShaderObject object) {
        super(getType(object.getType()), object.getId(), object.getLocation().toString());
        this.delegate = object;
    }

    private static Type getType(final ShaderType type) {
        return switch (type) {
            case VERTEX -> Type.VERTEX;
            case FRAGMENT -> Type.FRAGMENT;
            default -> throw new IllegalArgumentException("Unsupported shader type");
        };
    }

    @Override
    public int getId() {
        return delegate.getId();
    }

    @Override
    public @NotNull String getName() {
        return delegate.getLocation().toString();
    }

    @Override
    public void attachToShader(final @NotNull Shader shader) {
    }

    @Override
    public void close() {
    }
}
