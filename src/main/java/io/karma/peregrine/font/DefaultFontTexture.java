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

import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.api.font.Font;
import io.karma.peregrine.api.font.FontTexture;
import io.karma.peregrine.api.font.FontVariant;
import io.karma.peregrine.api.font.GlyphSprite;
import io.karma.peregrine.api.texture.TextureFilter;
import io.karma.peregrine.api.texture.TextureFormat;
import io.karma.peregrine.api.util.TextureUtils;
import io.karma.peregrine.api.texture.TextureWrapMode;
import io.karma.peregrine.api.util.Requires;
import io.karma.peregrine.texture.DefaultTexture;
import it.unimi.dsi.fastutil.chars.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.msdfgen.MSDFGen;
import org.lwjgl.util.msdfgen.MSDFGenBounds;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * @author Alexander Hinze
 * @since 04/05/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultFontTexture implements FontTexture {
    private final FontVariant font;
    private final int spriteSize;
    private final int sizeInSlots;
    private final int spriteBorder;
    private final float sdfRange;
    private final int renderType;
    private final float uScale;
    private final float vScale;
    private final DefaultGlyphSprite missingGlyphSprite;
    private final Image missingGlyphImage;
    private final Char2ObjectArrayMap<DefaultGlyphSprite> glyphSprites = new Char2ObjectArrayMap<>();
    private int textureId = -1;
    private boolean isReady = false;
    private float maxGlyphWidth;
    private float maxGlyphHeight;
    private float maxGlyphBearingX;
    private float maxGlyphBearingY;
    private float lineHeight;

    public DefaultFontTexture(final FontVariant font,
                              final int spriteSize,
                              final int spriteBorder,
                              final float sdfRange,
                              final int renderType) {
        this.font = font;
        this.spriteSize = spriteSize;
        this.spriteBorder = spriteBorder;
        this.sdfRange = sdfRange;
        this.renderType = renderType;

        // Set up missing glyph image
        final var missingGlyphImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        missingGlyphImage.setRGB(0, 0, 0xFF7700FF);
        missingGlyphImage.setRGB(1, 0, 0xFFFF00FF);
        missingGlyphImage.setRGB(1, 1, 0xFF7700FF);
        missingGlyphImage.setRGB(0, 1, 0xFFFF00FF);
        this.missingGlyphImage = missingGlyphImage.getScaledInstance(spriteSize,
            spriteSize,
            BufferedImage.SCALE_REPLICATE);

        // Simple way of finding a size that fits all characters but is a power of 2
        final var maxSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE) / spriteSize;
        final var numChars = font.getSupportedChars().toSet().size();
        int size = 2;
        while ((size * size) < numChars) {
            Requires.that(size < maxSize, "Font atlas too large, not supported yet");
            size <<= 1;
        }
        this.sizeInSlots = size;
        uScale = 1F / getWidth();
        vScale = 1F / getHeight();

        // @formatter:off
        missingGlyphSprite = new DefaultGlyphSprite(new DefaultGlyphMetrics(spriteSize, spriteSize, 0, 0, spriteSize, spriteSize, 0, 0),
            spriteSize, 0F, 0F);
        // @formatter:on
    }

    private void applyVariationAxes(final MSDFFont fontResource) {
        Peregrine.LOGGER.debug("Applying variation axes");
        final var variationAxes = fontResource.getVariationAxes();
        final var axisValues = this.font.getVariationAxes();
        for (final var axis : variationAxes) {
            final var name = axis.name();
            if (!axisValues.containsKey(name)) {
                continue;
            }
            final var value = axisValues.getFloat(name);
            Peregrine.LOGGER.debug("Overriding '{}' with {}", name, String.format("%.04f", value));
            fontResource.setVariationAxis(axis, value);
        }
    }

    private void clear() {
        glyphSprites.clear();
        maxGlyphWidth = 0;
        maxGlyphHeight = 0;
        maxGlyphBearingX = 0;
        maxGlyphBearingY = 0;
        lineHeight = 0;
    }

    private void buildAndMeasureShapes(final MSDFFont fontResource, final StitchContext context) {
        final var chars = font.getSupportedChars().toArray();
        final var numChars = chars.length;
        // Extract vector shape for every glyph and determine common scaling factor
        for (var i = 0; i < numChars; i++) {
            try (final var stack = MemoryStack.stackPush()) {
                final var c = chars[i];
                final var shape = fontResource.createGlyphShape(c);
                // Index 0 is always the exceptions since that's the space character
                if (i > 0 && (fontResource.isGlyphEmpty(c) || MSDFUtils.isShapeEmpty(shape))) {
                    MSDFGen.msdf_shape_free(shape); // Free shape right away
                    context.shapes.put(c, MemoryUtil.NULL);
                    continue;
                }
                final var boundsBuffer = MSDFGenBounds.malloc(stack);
                MSDFUtils.throwIfError(MSDFGen.msdf_shape_get_bounds(shape, boundsBuffer));
                final var width = boundsBuffer.r() - boundsBuffer.l();
                if (context.maxWidth < width) {
                    context.maxWidth = width;
                }
                final var height = boundsBuffer.t() - boundsBuffer.b();
                if (context.maxHeight < height) {
                    context.maxHeight = height;
                }
                context.shapes.put(c, shape);
            }
        }
    }

    private void renderShapes(final MSDFFont fontResource, final StitchContext context) {
        final var scale = context.getScale();
        var index = 0;
        final var shapeIterator = Char2LongMaps.fastIterable(context.shapes);
        for (final var entry : shapeIterator) {
            try (final var stack = MemoryStack.stackPush()) {
                final var shape = entry.getLongValue();
                final var atlasX = (index % sizeInSlots) * spriteSize;
                final var atlasY = (index / sizeInSlots) * spriteSize;
                if (shape == MemoryUtil.NULL) {
                    // Blit the missing glyph texture for all unsupported/empty slots
                    context.graphics.drawImage(missingGlyphImage, atlasX, atlasY, spriteSize, spriteSize, null);
                    index++;
                    continue;
                }
                MSDFUtils.scaleShape(shape, scale); // Scale to default size of font
                MSDFUtils.throwIfError(MSDFGen.msdf_shape_edge_colors_simple(shape, 3.0));

                final var totalSpriteBorder = spriteBorder << 1;
                final var actualSpriteSize = spriteSize - totalSpriteBorder;

                final var boundsBuffer = MSDFGenBounds.malloc(stack);
                MSDFUtils.throwIfError(MSDFGen.msdf_shape_get_bounds(shape, boundsBuffer));
                final var bbWidth = boundsBuffer.r() - boundsBuffer.l();
                final var bbHeight = boundsBuffer.t() - boundsBuffer.b();
                final var tx = -boundsBuffer.l();
                final var ty = -boundsBuffer.b() + (actualSpriteSize - bbHeight);
                final var sdfTx = tx + ((double) (actualSpriteSize >> 1) - (bbWidth * 0.5)) + spriteBorder;
                final var sdfTy = ty - ((double) (actualSpriteSize >> 1) - (bbHeight * 0.5)) + spriteBorder;
                // @formatter:off
                MSDFUtils.renderShapeToImage(renderType, spriteSize, spriteSize, shape,
                    1.0, 1.0, sdfTx, sdfTy, sdfRange,
                    context.image, atlasX, atlasY);
                // @formatter:on

                final var c = entry.getCharKey();
                final var metrics = fontResource.createGlyphMetrics(c, scale);
                if (metrics != null) {
                    final var width = metrics.getWidth();
                    final var height = metrics.getHeight();
                    final var bearingX = metrics.getBearingX();
                    final var bearingY = metrics.getBearingY();
                    final var atlasImage = context.image;
                    final var u = (1F / atlasImage.getWidth()) * atlasX;
                    final var v = (1F / atlasImage.getHeight()) * atlasY;
                    if (maxGlyphWidth < width) {
                        maxGlyphWidth = width;
                    }
                    if (maxGlyphHeight < height) {
                        maxGlyphHeight = height;
                    }
                    if (maxGlyphBearingX < bearingX) {
                        maxGlyphBearingX = bearingX;
                    }
                    if (maxGlyphBearingY < bearingY) {
                        maxGlyphBearingY = bearingY;
                    }
                    glyphSprites.put(c, new DefaultGlyphSprite(metrics, spriteSize, u, v));
                }

                MSDFGen.msdf_shape_free(shape);
                index++;
            }
        }
    }

    private void fillEmptySlots(final StitchContext context) {
        final var numShapes = context.shapes.size();
        final var numEmptySlots = (sizeInSlots * sizeInSlots) - numShapes;
        for (var i = 0; i < numEmptySlots; i++) {
            final var contIndex = numShapes + i;
            context.graphics.drawImage(missingGlyphImage,
                (contIndex % sizeInSlots) * spriteSize,
                (contIndex / sizeInSlots) * spriteSize,
                null);
        }
    }

    @Override
    public TextureFormat getFormat() {
        return DefaultTexture.FORMAT;
    }

    @Override
    public TextureFilter getMinFilter() {
        return TextureFilter.NEAREST;
    }

    @Override
    public TextureFilter getMagFilter() {
        return TextureFilter.NEAREST;
    }

    @Override
    public TextureWrapMode getHorizontalWrapMode() {
        return TextureWrapMode.CLAMP;
    }

    @Override
    public TextureWrapMode getVerticalWrapMode() {
        return TextureWrapMode.CLAMP;
    }

    @Override
    public void prepare(final ResourceProvider resourceProvider) {
        isReady = false;
        if (textureId != -1) {
            GL11.glDeleteTextures(textureId);
        }
        textureId = TextureUtils.createDefaultTexture();
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
        final var fontLocation = font.getLocation();
        Peregrine.LOGGER.debug("Rebuilding font atlas for font {} with {}x{} slots",
            fontLocation,
            sizeInSlots,
            sizeInSlots);
        clear();

        final var context = new StitchContext();

        try (final var fontResource = new MSDFFont(resourceProvider.getResourceOrThrow(fontLocation).open())) {
            applyVariationAxes(fontResource);
            buildAndMeasureShapes(fontResource, context);
            renderShapes(fontResource, context);

            final var scale = context.getScale();
            final var face = fontResource.getFace();
            final var ascender = (int) (scale * FreeTypeUtils.f26Dot6ToFP32(face.ascender()));
            final var descender = (int) (scale * FreeTypeUtils.f26Dot6ToFP32(face.descender()));
            lineHeight = ascender - descender;
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not load glyph data for font {}: {}", fontLocation, error);
        }

        fillEmptySlots(context);
        context.graphics.dispose();
        if (PeregrineMod.isDevelopmentEnvironment()) {
            TextureUtils.save(context.image,
                new ResourceLocation(Peregrine.MODID, String.format("%s.png", font.getVariantString())));
        }

        TextureUtils.uploadTexture(textureId, context.image);
        isReady = true;
    }

    @Override
    public void dispose() {
        GL11.glDeleteTextures(textureId);
    }

    @Override
    public int getWidth() {
        return sizeInSlots * spriteSize;
    }

    @Override
    public int getHeight() {
        return sizeInSlots * spriteSize;
    }

    @Override
    public int getSpriteSize() {
        return spriteSize;
    }

    @Override
    public int getSpriteBorder() {
        return spriteBorder;
    }

    @Override
    public float getMaxGlyphWidth() {
        return maxGlyphWidth;
    }

    @Override
    public float getMaxGlyphHeight() {
        return maxGlyphHeight;
    }

    @Override
    public float getMaxGlyphBearingX() {
        return maxGlyphBearingX;
    }

    @Override
    public float getMaxGlyphBearingY() {
        return maxGlyphBearingY;
    }

    @Override
    public float getLineHeight() {
        return lineHeight;
    }

    @Override
    public float getUScale() {
        return uScale;
    }

    @Override
    public float getVScale() {
        return vScale;
    }

    @Override
    public CharSet getSupportedChars() {
        return font.getSupportedChars().toSet();
    }

    @Override
    public int getId() {
        return textureId;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public synchronized GlyphSprite getGlyphSprite(final char c) {
        return glyphSprites.getOrDefault(c, missingGlyphSprite);
    }

    @Override
    public synchronized Char2ObjectMap<GlyphSprite> getGlyphSprites() {
        return Char2ObjectMaps.unmodifiable(glyphSprites);
    }

    @Override
    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
    }

    @Override
    public void unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public float getSDFRange() {
        return sdfRange;
    }

    // Ensures the memoize function can obtain a proper hash
    @Override
    public int hashCode() {
        return Objects.hash(font.getLocation(), spriteSize, sizeInSlots, spriteBorder, sdfRange, renderType);
    }

    @Override
    public String toString() {
        return String.format("DefaultFontAtlas[textureId=%d,spriteSize=%d,sizeInSlots=%d]",
            textureId,
            spriteBorder,
            sizeInSlots);
    }

    private final class StitchContext {
        final Char2LongLinkedOpenHashMap shapes = new Char2LongLinkedOpenHashMap();
        final BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics graphics = image.createGraphics();
        double maxWidth = 0.0;
        double maxHeight = 0.0;

        float getScale() {
            final var totalSpriteBorder = spriteBorder << 1;
            final var actualSpriteSize = spriteSize - totalSpriteBorder;
            return (float) actualSpriteSize / (float) Math.max(maxWidth, maxHeight);
        }
    }
}
