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

import io.karma.peregrine.Peregrine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.util.function.IntSupplier;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DynamicSampler implements Sampler {
    private final int id;
    private final String name;
    private IntSupplier textureId = () -> -1;

    public DynamicSampler(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public void setTextureId(final IntSupplier textureId) {
        this.textureId = textureId;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setup(final ShaderProgram program) {
        Peregrine.LOGGER.debug("Creating dynamic sampler '{}'/{} for program {}", name, id, program.getId());
        GL20.glUseProgram(program.getId());
        GL20.glUniform1i(program.getUniformLocation(name), id);
        GL20.glUseProgram(0);
    }

    @Override
    public void bind(final ShaderProgram program) {
        final var textureId = this.textureId.getAsInt();
        if (textureId <= 0) {
            return;
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + id);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    @Override
    public void unbind(final ShaderProgram program) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + id);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    @Override
    public String toString() {
        return String.format("DynamicSampler[textureId=%d]", textureId.getAsInt());
    }
}
