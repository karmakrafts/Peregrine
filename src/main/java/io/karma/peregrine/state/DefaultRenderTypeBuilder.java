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

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.shader.DefaultShaderProgramBuilder;
import io.karma.peregrine.shader.ShaderProgram;
import io.karma.peregrine.shader.ShaderProgramBuilder;
import io.karma.peregrine.target.RenderTarget;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.EmptyTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.function.Consumer;

/**
 * @author Alexander Hinze
 * @since 07/09/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultRenderTypeBuilder implements RenderTypeBuilder {
    private static final RenderCallback IDENTITY_CALLBACK = () -> {
    };

    private String name;
    private VertexFormat vertexFormat;
    private Mode mode = Mode.TRIANGLES;
    private int bufferSize = 128;
    private RenderTarget target = PeregrineMod.RENDER_TARGET_FACTORIES.getMainTarget();
    private ShaderProgram shader;
    private BlendMode blendMode = Transparency.NONE;
    private boolean affectsCrumbling;
    private boolean sorting;
    private boolean outline;
    private boolean lightmap;
    private boolean overlay;
    private boolean culling = true;
    private RenderCallback onPreRender = IDENTITY_CALLBACK;
    private RenderCallback onPostRender = IDENTITY_CALLBACK;

    public static RenderType build(final Consumer<RenderTypeBuilder> callback) {
        final var builder = new DefaultRenderTypeBuilder();
        callback.accept(builder);
        return builder.build();
    }

    private static TransparencyStateShard getTransparencyState(final BlendMode blendMode) {
        if (!blendMode.usesBlending()) {
            return RenderStateShard.NO_TRANSPARENCY;
        }
        if (blendMode == Transparency.TRANSPARENCY) {
            return RenderStateShard.TRANSLUCENT_TRANSPARENCY;
        }
        return new TransparencyStateShard(blendMode.toString(), () -> {
            GL11.glEnable(GL11.GL_BLEND);
            GL20.glBlendFuncSeparate(blendMode.getColorSourceFactor().value,
                blendMode.getColorDestFactor().value,
                blendMode.getSourceFactor().value,
                blendMode.getDestFactor().value);
        }, () -> GL11.glDisable(GL11.GL_BLEND));
    }

    RenderType build() {
        Preconditions.checkArgument(name != null, "Name must be specified");
        Preconditions.checkArgument(vertexFormat != null, "Vertex format must be specified");
        Preconditions.checkArgument(shader != null, "Shader must be specified");
        // @formatter:off
        return RenderType.create(name, vertexFormat, mode, bufferSize, affectsCrumbling, sorting,
            CompositeState.builder()
                .setShaderState(shader.asStateShard())
                .setOutputState(target.asStateShard())
                .setCullState(culling ? RenderStateShard.CULL : RenderStateShard.NO_CULL)
                .setLightmapState(lightmap ? RenderStateShard.LIGHTMAP : RenderStateShard.NO_LIGHTMAP)
                .setOverlayState(overlay ? RenderStateShard.OVERLAY : RenderStateShard.NO_OVERLAY)
                .setTransparencyState(getTransparencyState(blendMode))
                .setTexturingState(RenderStateShard.DEFAULT_TEXTURING)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .setTextureState(new EmptyTextureStateShard(onPreRender, onPostRender))
                .createCompositeState(outline)
        );
        // @formatter:on
    }

    @Override
    public RenderTypeBuilder name(final String name) {
        this.name = name;
        return this;
    }

    @Override
    public RenderTypeBuilder mode(final Mode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public RenderTypeBuilder vertexFormat(final VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;
        return this;
    }

    @Override
    public RenderTypeBuilder bufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    @Override
    public RenderTypeBuilder blendMode(final BlendMode blendMode) {
        this.blendMode = blendMode;
        return this;
    }

    @Override
    public RenderTypeBuilder sorting(final boolean sorting) {
        this.sorting = sorting;
        return this;
    }

    @Override
    public RenderTypeBuilder affectsCrumbling(final boolean affectsCrumbling) {
        this.affectsCrumbling = affectsCrumbling;
        return this;
    }

    @Override
    public RenderTypeBuilder shader(final ShaderProgram program) {
        shader = program;
        return this;
    }

    @Override
    public RenderTypeBuilder shader(final Consumer<ShaderProgramBuilder> callback) {
        shader = DefaultShaderProgramBuilder.build(builder -> callback.accept(builder.format(vertexFormat)));
        return this;
    }

    @Override
    public RenderTypeBuilder target(final RenderTarget target) {
        this.target = target;
        return this;
    }

    @Override
    public RenderTypeBuilder culling(final boolean culling) {
        this.culling = culling;
        return this;
    }

    @Override
    public RenderTypeBuilder lightmap(final boolean lightmap) {
        this.lightmap = lightmap;
        return this;
    }

    @Override
    public RenderTypeBuilder overlay(final boolean overlay) {
        this.overlay = overlay;
        return this;
    }

    @Override
    public RenderTypeBuilder outline(final boolean outline) {
        this.outline = outline;
        return this;
    }

    @Override
    public RenderTypeBuilder bufferSizeInVertices(final int numVertices) {
        Preconditions.checkArgument(vertexFormat != null, "Vertex format must be specified");
        bufferSize = numVertices * vertexFormat.getVertexSize();
        return this;
    }

    @Override
    public RenderTypeBuilder onPreRender(final RenderCallback callback) {
        if (onPreRender == IDENTITY_CALLBACK) {
            onPreRender = callback;
            return this;
        }
        onPreRender = onPreRender.andThen(callback);
        return this;
    }

    @Override
    public RenderTypeBuilder onPostRender(final RenderCallback callback) {
        if (onPostRender == IDENTITY_CALLBACK) {
            onPostRender = callback;
            return this;
        }
        onPostRender = onPostRender.andThen(callback);
        return this;
    }
}
