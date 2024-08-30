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

package io.karma.peregrine.shader;

import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.Shader;
import io.karma.peregrine.Peregrine;
import io.karma.peregrine.util.HashUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultShaderObject extends Program implements ShaderObject {
    private final ShaderType type;
    private final ResourceLocation location;
    private final Supplier<ShaderPreProcessor> shaderPreProcessorSupplier;
    private boolean isCompiled;

    DefaultShaderObject(final ShaderType type,
                        final ResourceLocation location,
                        final Supplier<ShaderPreProcessor> shaderPreProcessorSupplier) {
        super(getType(type), GL20.glCreateShader(type.getGLType()), location.toString());
        this.type = type;
        this.location = location;
        this.shaderPreProcessorSupplier = shaderPreProcessorSupplier;
        Peregrine.LOGGER.debug("Created new shader object {}", id);
    }

    private static Type getType(final ShaderType type) {
        return switch (type) {
            case VERTEX -> Type.VERTEX;
            case FRAGMENT -> Type.FRAGMENT;
            default -> throw new IllegalArgumentException("Unsupported shader type");
        };
    }

    @Override
    public boolean recompile(final Path directory,
                             final ShaderProgram program,
                             final ResourceProvider resourceProvider) {
        final var loader = program.getLoader();
        detach(program);

        if (!loader.load(directory, resourceProvider, program, this)) {
            GL20.glCompileShader(id);
            if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                Peregrine.LOGGER.error("Could not recompile shader {}: {}", location, GL20.glGetShaderInfoLog(id));
                return false; // Link is always cancelled in this case
            }
            loader.save(directory, resourceProvider, program, this);
        }

        isCompiled = true;
        attach(program);
        return true; // Link may occur
    }

    @Override
    public ShaderPreProcessor getPreProcessor() {
        return shaderPreProcessorSupplier.get();
    }

    @Override
    public void attachToShader(final @NotNull Shader shader) {
    }

    @Override
    public void close() {
    }

    @Override
    public void attach(final ShaderProgram program) {
        if (program.isAttached(this)) {
            return;
        }
        GL20.glAttachShader(program.getId(), id);
    }

    @Override
    public void detach(final ShaderProgram program) {
        if (!program.isAttached(this)) {
            return;
        }
        GL20.glDetachShader(program.getId(), id);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public ShaderType getType() {
        return type;
    }

    @Override
    public boolean isCompiled() {
        return isCompiled;
    }

    @Override
    public String toString() {
        return String.format("DefaultShaderObject[id=%d,location=%s]", id, location);
    }

    @Override
    public int hashCode() {
        return HashUtils.combine(type.ordinal(), location.hashCode());
    }
}
