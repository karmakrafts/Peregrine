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

import com.mojang.blaze3d.vertex.VertexFormat;
import io.karma.peregrine.Peregrine;
import io.karma.peregrine.buffer.UniformBuffer;
import io.karma.peregrine.dispose.Disposable;
import io.karma.peregrine.reload.Reloadable;
import io.karma.peregrine.uniform.UniformCache;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface ShaderProgram extends Disposable, Reloadable {
    static ShaderProgram create(final Consumer<ShaderProgramBuilder> callback) {
        return Peregrine.getShaderProgramFactory().apply(callback);
    }

    int getId();

    List<ShaderObject> getObjects();

    ShaderObject getObject(final ShaderType type);

    ShaderLoader getLoader();

    VertexFormat getVertexFormat();

    ShaderStateShard asStateShard();

    void bind();

    void unbind();

    int getUniformLocation(final String name);

    int getUniformBlockIndex(final String name);

    void setSampler(final String name, final int textureId);

    void setSampler(final String name, final ResourceLocation location);

    Sampler getSampler(final String name);

    boolean isLinked();

    void requestRelink();

    boolean isRelinkRequested();

    Map<String, Object> getConstants();

    Map<String, Object> getDefines();

    boolean isAttached(final ShaderObject object);

    UniformCache getUniforms();

    Map<String, UniformBuffer> getUniformBuffers();
}
