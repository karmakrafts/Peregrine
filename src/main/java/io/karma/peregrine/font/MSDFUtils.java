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

import io.karma.peregrine.Peregrine;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2d;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.msdfgen.*;

import java.awt.image.BufferedImage;

/**
 * @author Alexander Hinze
 * @since 04/05/2024
 */
@OnlyIn(Dist.CLIENT)
public final class MSDFUtils {
    // @formatter:off
    private MSDFUtils() {}
    // @formatter:on

    public static void rewindShapeIfNeeded(final long shape) {
        try (final var stack = MemoryStack.stackPush()) {
            final var bounds = MSDFGenBounds.malloc(stack);
            MSDFUtils.throwIfError(MSDFGen.msdf_shape_get_bounds(shape, bounds));
            final var outerPoint = new Vector2d(bounds.l() - (bounds.r() - bounds.l()) - 1,
                bounds.b() - (bounds.t() - bounds.b()) - 1);
            final var distanceBuffer = stack.mallocDouble(1);
            MSDFUtils.throwIfError(MSDFGen.msdf_shape_one_shot_distance(shape,
                MSDFGenVector2.malloc(stack).x(outerPoint.x).y(outerPoint.y),
                distanceBuffer));
            if (distanceBuffer.get() > 0.0) {
                Peregrine.LOGGER.debug("Shape wound incorrectly, correcting winding order");
                final var contourCountBuffer = stack.mallocPointer(1);
                MSDFUtils.throwIfError(MSDFGen.msdf_shape_get_contour_count(shape, contourCountBuffer));
                final var contourCount = contourCountBuffer.get();
                for (long i = 0; i < contourCount; i++) {
                    try (final var contourStack = MemoryStack.stackPush()) {
                        final var contourAddressBuffer = contourStack.mallocPointer(1);
                        MSDFUtils.throwIfError(MSDFGen.msdf_shape_get_contour(shape, i, contourAddressBuffer));
                        MSDFUtils.throwIfError(MSDFGen.msdf_contour_reverse(Checks.check(contourAddressBuffer.get())));
                    }
                }
            }
        }
    }

    public static boolean isShapeEmpty(final long shape) {
        try (final var stack = MemoryStack.stackPush()) {
            final var countBuffer = stack.mallocPointer(1);
            throwIfError(MSDFGen.msdf_shape_get_contour_count(shape, countBuffer));
            return countBuffer.get() == 0;
        }
    }

    public static void scaleShape(final long shape, final double scale) {
        try (final var stack = MemoryStack.stackPush()) {
            final var countBuffer = stack.mallocPointer(1);
            throwIfError(MSDFGen.msdf_shape_get_contour_count(shape, countBuffer));
            final var contourCount = countBuffer.get();
            for (long i = 0; i < contourCount; i++) {
                try (final var contourStack = MemoryStack.stackPush()) {
                    final var contourAddressBuffer = contourStack.mallocPointer(1);
                    throwIfError(MSDFGen.msdf_shape_get_contour(shape, i, contourAddressBuffer));
                    final var contour = Checks.check(contourAddressBuffer.get());
                    final var edgeCountBuffer = contourStack.mallocPointer(1);
                    throwIfError(MSDFGen.msdf_contour_get_edge_count(contour, edgeCountBuffer));
                    final var edgeCount = edgeCountBuffer.get();
                    for (long j = 0; j < edgeCount; j++) {
                        try (final var edgeStack = MemoryStack.stackPush()) {
                            final var edgeAddressBuffer = edgeStack.mallocPointer(1);
                            throwIfError(MSDFGen.msdf_contour_get_edge(contour, j, edgeAddressBuffer));
                            final var segment = Checks.check(edgeAddressBuffer.get());
                            final var pointCountBuffer = edgeStack.mallocPointer(1);
                            throwIfError(MSDFGen.msdf_segment_get_point_count(segment, pointCountBuffer));
                            final var pointCount = pointCountBuffer.get();
                            for (long k = 0; k < pointCount; k++) {
                                try (final var pointStack = MemoryStack.stackPush()) {
                                    final var pos = MSDFGenVector2.malloc(pointStack);
                                    throwIfError(MSDFGen.msdf_segment_get_point(segment, k, pos));
                                    pos.x(pos.x() * scale);
                                    pos.y(pos.y() * scale);
                                    throwIfError(MSDFGen.msdf_segment_set_point(segment, k, pos));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void throwIfError(final int result) {
        if (result != MSDFGen.MSDF_SUCCESS) {
            switch (result) {
                case MSDFGen.MSDF_ERR_FAILED:
                    throw new IllegalStateException("Operation failed");
                case MSDFGen.MSDF_ERR_INVALID_ARG:
                    throw new IllegalStateException("Invalid argument");
                case MSDFGen.MSDF_ERR_INVALID_TYPE:
                    throw new IllegalStateException("Invalid type");
                case MSDFGen.MSDF_ERR_INVALID_SIZE:
                    throw new IllegalStateException("Invalid size");
                case MSDFGen.MSDF_ERR_INVALID_INDEX:
                    throw new IllegalStateException("Invalid index");
                default:
                    throw new IllegalStateException("Unknown error");
            }
        }
    }

    public static void blitBitmapToImage(final MSDFGenBitmap bitmap,
                                         final BufferedImage image,
                                         final int dstX,
                                         final int dstY) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new IllegalArgumentException("Invalid target image format");
        }
        try (final var stack = MemoryStack.stackPush()) {
            final var width = bitmap.width();
            final var height = bitmap.height();

            // Retrieve channel count
            final var channelCountBuffer = stack.mallocInt(1);
            throwIfError(MSDFGen.msdf_bitmap_get_channel_count(bitmap, channelCountBuffer));
            final var channelCount = channelCountBuffer.get();

            // Retrieve pixel data address
            final var pixelAddressBuffer = stack.mallocPointer(1);
            throwIfError(MSDFGen.msdf_bitmap_get_pixels(bitmap, pixelAddressBuffer));
            final var srcAddress = Checks.check(pixelAddressBuffer.get());

            // Select bitmap sampler based on channelCount
            final BitmapSampler sampler = switch (channelCount) {
                case 3 -> BitmapSampler.MSDF;
                case 4 -> BitmapSampler.MTSDF;
                default -> BitmapSampler.SDF;
            };

            // Copy image data
            final var pixelSize = channelCount * Float.BYTES;
            for (var y = 0; y < height; y++) {
                for (var x = 0; x < width; x++) {
                    final var srcIndex = (long) y * width + x;
                    final var srcPixelAddress = srcAddress + (pixelSize * srcIndex);
                    image.setRGB(dstX + x, dstY + (height - y - 1), sampler.sample(srcPixelAddress));
                }
            }
        }
    }

    public static void renderShapeToImage(final int type,
                                          final int width,
                                          final int height,
                                          final long shape,
                                          final double xScale,
                                          final double yScale,
                                          final double xOffset,
                                          final double yOffset,
                                          final double range,
                                          final BufferedImage dst,
                                          final int dstX,
                                          final int dstY) {
        try (final var stack = MemoryStack.stackPush()) {
            final var bitmap = MSDFGenBitmap.malloc(stack);
            throwIfError(MSDFGen.msdf_bitmap_alloc(type, width, height, bitmap));
            final var transform = MSDFGenTransform.malloc(stack);
            transform.scale().set(xScale, yScale);
            transform.translation().set(xOffset, yOffset);
            transform.distance_mapping().set(-0.5 * range, 0.5 * range);
            switch (type) {
                case MSDFGen.MSDF_BITMAP_TYPE_SDF:
                    throwIfError(MSDFGen.msdf_generate_sdf(bitmap, shape, transform));
                    break;
                case MSDFGen.MSDF_BITMAP_TYPE_PSDF:
                    throwIfError(MSDFGen.msdf_generate_psdf(bitmap, shape, transform));
                    break;
                case MSDFGen.MSDF_BITMAP_TYPE_MSDF:
                    throwIfError(MSDFGen.msdf_generate_msdf(bitmap, shape, transform));
                    break;
                case MSDFGen.MSDF_BITMAP_TYPE_MTSDF:
                    throwIfError(MSDFGen.msdf_generate_mtsdf(bitmap, shape, transform));
                    break;
            }
            blitBitmapToImage(bitmap, dst, dstX, dstY);
            MSDFGen.msdf_bitmap_free(bitmap);
        }
    }

    @FunctionalInterface
    private interface BitmapSampler {
        BitmapSampler SDF = (address) -> {
            final var r = getAndQuantize(address, 0);
            return (0xFF << 24) | (r << 16) | (r << 8) | r;
        };
        BitmapSampler MSDF = (address) -> {
            final var r = getAndQuantize(address, 0);
            final var g = getAndQuantize(address, 1);
            final var b = getAndQuantize(address, 2);
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        };
        BitmapSampler MTSDF = (address) -> {
            final var r = getAndQuantize(address, 0);
            final var g = getAndQuantize(address, 1);
            final var b = getAndQuantize(address, 2);
            final var a = getAndQuantize(address, 3);
            return (a << 24) | (r << 16) | (g << 8) | b;
        };

        static int getAndQuantize(final long address, final int channelIndex) {
            final var raw = MemoryUtil.memGetFloat(address + ((long) Float.BYTES * channelIndex));
            return ~(int) (255.5F - 255F * Mth.clamp(raw, 0F, 1F)) & 0xFF;
        }

        int sample(final long address);
    }
}
