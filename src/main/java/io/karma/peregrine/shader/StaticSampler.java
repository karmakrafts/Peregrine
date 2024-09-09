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

import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.api.dispose.Disposable;
import io.karma.peregrine.api.dispose.DisposePriority;
import io.karma.peregrine.api.reload.PreparePriority;
import io.karma.peregrine.api.reload.ReloadPriority;
import io.karma.peregrine.api.reload.Reloadable;
import io.karma.peregrine.api.shader.Sampler;
import io.karma.peregrine.api.shader.ShaderProgram;
import io.karma.peregrine.api.texture.Texture;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.ARBBindlessTexture;
import org.lwjgl.opengl.GL20;

import java.util.function.IntSupplier;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
@ReloadPriority(-100)
@PreparePriority(-100)
@DisposePriority(100)
public final class StaticSampler implements Sampler, Reloadable, Disposable {
    private static final long INVALID_HANDLE = 0;
    private final String name;
    private final IntSupplier textureId;
    private int id;

    private StaticSampler(final int id, final String name, final IntSupplier textureId) {
        this.id = id;
        this.name = name;
        this.textureId = textureId;
        PeregrineMod.DISPOSE_HANDLER.register(this);
        PeregrineMod.RELOAD_HANDLER.register(this);
    }

    /**
     * This handles creating static samplers with a safe fallback to {@link DynamicSampler}
     * if the GL_ARB_bindless_texture extension is not supported.
     *
     * @param textureId The texture ID to create a static sampler for.
     * @return A new instance of {@link StaticSampler} if the GL_ARB_bindless_texture extension is present,
     * a new instance of {@link DynamicSampler} with the given texture ID pre-setup otherwise.
     */
    public static Sampler create(final int id, final String name, final IntSupplier textureId) {
        if (!Peregrine.supportsBindlessTextures()) {
            final var sampler = new DynamicSampler(id, name);
            sampler.setTextureId(textureId);
            return sampler;
        }
        return new StaticSampler(id, name, textureId);
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    private long getHandle() {
        final var id = textureId.getAsInt();
        if (id == Texture.INVALID_ID) {
            return INVALID_HANDLE;
        }
        return ARBBindlessTexture.glGetTextureHandleARB(id);
    }

    @Override
    public void setup(final ShaderProgram program) {
        Peregrine.LOGGER.debug("Creating static sampler '{}'/{} for program {}", name, id, program.getId());

        final var handle = getHandle();
        if (handle == 0) {
            return;
        }
        if (!ARBBindlessTexture.glIsTextureHandleResidentARB(handle)) {
            ARBBindlessTexture.glMakeTextureHandleResidentARB(handle);
        }

        final var location = program.getUniformLocation(name);
        GL20.glUseProgram(program.getId());
        ARBBindlessTexture.glUniformHandleui64ARB(location, handle);
        GL20.glUseProgram(0);
    }

    @Override
    public void bind(final ShaderProgram program) {
    }

    @Override
    public void unbind(final ShaderProgram program) {
    }

    @Override
    public void dispose() {
        if (id == INVALID_ID) {
            return;
        }
        final var handle = getHandle();
        if (handle == INVALID_HANDLE) {
            return;
        }
        if (ARBBindlessTexture.glIsTextureHandleResidentARB(handle)) {
            ARBBindlessTexture.glMakeTextureHandleNonResidentARB(handle);
        }
        id = INVALID_ID;
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
    }

    @Override
    public void prepare(final ResourceProvider resourceProvider) {
        dispose();
    }

    @Override
    public String toString() {
        return String.format("StaticSampler[textureId=%d,handle=%d]", textureId.getAsInt(), getHandle());
    }
}
