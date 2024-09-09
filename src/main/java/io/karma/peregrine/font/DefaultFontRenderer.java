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

package io.karma.peregrine.font;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.api.color.ColorProvider;
import io.karma.peregrine.api.font.DistanceFieldType;
import io.karma.peregrine.api.font.Font;
import io.karma.peregrine.api.font.FontRenderer;
import io.karma.peregrine.api.font.FontTexture;
import io.karma.peregrine.api.reload.Reloadable;
import io.karma.peregrine.shader.DefaultShaderProgramBuilder;
import io.karma.peregrine.api.shader.ShaderProgram;
import io.karma.peregrine.api.shader.ShaderType;
import io.karma.peregrine.state.DefaultRenderTypeBuilder;
import io.karma.peregrine.api.state.Transparency;
import io.karma.peregrine.api.target.RenderTarget;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.util.msdfgen.MSDFGen;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * @author Alexander Hinze
 * @since 01/09/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultFontRenderer implements FontRenderer, Reloadable {
    // @formatter:off
    private static final ShaderProgram SHADER = DefaultShaderProgramBuilder.build(it -> it
        .stage(it2 -> it2
            .type(ShaderType.VERTEX)
            .location(Peregrine.MODID, "shaders/font.vert.glsl")
        )
        .stage(it2 -> it2
            .type(ShaderType.FRAGMENT)
            .location(Peregrine.MODID, "shaders/font.frag.glsl")
        )
        .globalUniforms()
    );
    // @formatter:on

    private final RenderTarget renderTarget;
    private final Function<FontRenderContext, RenderType> renderType;
    private final HashMap<FontTextureKey, FontTexture> fontTextures = new HashMap<>();

    public DefaultFontRenderer(final RenderTarget renderTarget) {
        this.renderTarget = renderTarget;
        // @formatter:off
        renderType = Util.memoize(ctx -> {
            final var location = ctx.texture.getFont().getLocation();
            return DefaultRenderTypeBuilder.build(it -> it
                .name(String.format("font_%s_%s", location.getNamespace(), location.getPath().replace('/', '_')))
                .vertexFormat(DefaultVertexFormat.POSITION_COLOR_TEX)
                .mode(Mode.TRIANGLES)
                .blendMode(Transparency.TRANSPARENCY)
                .bufferSizeInVertices(1024)
                .culling(false)
                .sorting(true)
                .shader(SHADER)
            );
        });
        // @formatter:on
        PeregrineMod.RELOAD_HANDLER.register(this);
    }

    private static int getBitmapType(final DistanceFieldType type) {
        return switch (type) {
            case PSDF -> MSDFGen.MSDF_BITMAP_TYPE_PSDF;
            case MSDF -> MSDFGen.MSDF_BITMAP_TYPE_MSDF;
            case MTSDF -> MSDFGen.MSDF_BITMAP_TYPE_MTSDF;
            default -> MSDFGen.MSDF_BITMAP_TYPE_SDF;
        };
    }

    @Override
    public FontTexture getFontTexture(final Font font) {
        return fontTextures.computeIfAbsent(new FontTextureKey(font.getLocation(), font.getVariationAxes()),
            location -> {
                final var family = font.getFamily();
                return new DefaultFontTexture(font.asVariant(),
                    family.getGlyphSpriteSize(),
                    family.getGlyphSpriteBorder(),
                    family.getDistanceFieldRange(),
                    getBitmapType(family.getDistanceFieldType()));
            });
    }

    @Override
    public int getLineHeight(final Font font) {
        final var fontVariant = font.asVariant();
        final var atlas = getFontTexture(fontVariant);
        final var scale = fontVariant.getSize() / atlas.getMaxGlyphHeight();
        return (int) (scale * atlas.getLineHeight());
    }

    @Override
    public int getStringWidth(final Font font, final CharSequence s) {
        final var fontVariant = font.asVariant();
        final var atlas = getFontTexture(fontVariant);
        final var scale = fontVariant.getSize() / atlas.getMaxGlyphHeight();
        var width = 0F;
        for (var i = 0; i < s.length(); i++) {
            final var metrics = atlas.getGlyphSprite(s.charAt(i)).getMetrics();
            width += scale * metrics.getAdvanceX();
        }
        return (int) width;
    }

    @Override
    public int render(final int x, final int y, final char c, final Font font, final ColorProvider color) {
        return 0;
    }

    @Override
    public int render(final int x, final int y, final CharSequence text, final Font font, final ColorProvider color) {
        return 0;
    }

    @Override
    public int render(final int x, final int y, final char c, final Font font, final IntFunction<ColorProvider> color) {
        return 0;
    }

    @Override
    public int render(final int x,
                      final int y,
                      final CharSequence text,
                      final Font font,
                      final IntFunction<ColorProvider> color) {
        return 0;
    }

    @Override
    public RenderTarget getRenderTarget() {
        return renderTarget;
    }

    @Override
    public void prepare(final ResourceProvider resourceProvider) {
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
    }

    private record FontTextureKey(ResourceLocation location, Object2FloatMap<String> variationAxes) {
    }

    private record FontRenderContext(FontTexture texture, float scale) {
    }
}
