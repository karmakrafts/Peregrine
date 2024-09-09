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

package io.karma.peregrine.state;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.karma.peregrine.shader.ShaderProgram;
import io.karma.peregrine.shader.ShaderProgramBuilder;
import io.karma.peregrine.target.RenderTarget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

/**
 * @author Alexander Hinze
 * @since 07/09/2024
 */
@OnlyIn(Dist.CLIENT)
public interface RenderTypeBuilder {
    RenderTypeBuilder name(final String name);

    RenderTypeBuilder vertexFormat(final VertexFormat vertexFormat);

    RenderTypeBuilder mode(final Mode mode);

    RenderTypeBuilder bufferSize(final int bufferSize);

    RenderTypeBuilder bufferSizeInVertices(final int numVertices);

    RenderTypeBuilder blendMode(final BlendMode blendMode);

    RenderTypeBuilder sorting(final boolean sorting);

    RenderTypeBuilder affectsCrumbling(final boolean affectsCrumbling);

    RenderTypeBuilder shader(final ShaderProgram program);

    RenderTypeBuilder shader(final Consumer<ShaderProgramBuilder> callback);

    RenderTypeBuilder target(final RenderTarget target);

    RenderTypeBuilder culling(final boolean culling);

    RenderTypeBuilder lightmap(final boolean lightmap);

    RenderTypeBuilder overlay(final boolean overlay);

    RenderTypeBuilder outline(final boolean outline);

    RenderTypeBuilder layering(final Layering layering);

    RenderTypeBuilder depthTest(final DepthTest depthTest);

    RenderTypeBuilder onPreRender(final RenderCallback callback);

    RenderTypeBuilder onPostRender(final RenderCallback callback);
}
