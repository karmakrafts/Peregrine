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

import io.karma.peregrine.api.font.FontVariationAxis;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_MM_Var;
import org.lwjgl.util.freetype.FreeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class FreeTypeUtils {
    private static final double F16DOT16_SCALE = 65536.0;
    private static final double F26DOT6_SCALE = 64.0;

    // @formatter:off
    private FreeTypeUtils() {}
    // @formatter:on

    // F16DOT16 conversions

    public static float f16Dot16ToFP32(final long value) {
        return (float) ((1.0 / F16DOT16_SCALE) * (double) value);
    }

    public static long fp32ToF16Dot16(final float value) {
        return (long) (value * F16DOT16_SCALE);
    }

    // F26DOT6 conversions

    public static float f26Dot6ToFP32(final long value) {
        return (float) ((1.0 / F26DOT6_SCALE) * (float) value);
    }

    public static long fp32ToF26Dot6(final float value) {
        return (long) (value * F26DOT6_SCALE);
    }

    // Font variation axes - adapted from https://github.com/Chlumsky/msdfgen/blob/master/ext/import-font.cpp#L269-L311

    public static List<FontVariationAxis> listFontVariationAxes(final long library, final FT_Face face) {
        try (final var stack = MemoryStack.stackPush()) {
            final var faceFlags = face.face_flags();
            if ((faceFlags & FreeType.FT_FACE_FLAG_MULTIPLE_MASTERS) == 0) {
                return Collections.emptyList();
            }
            final var masterAddressBuffer = stack.mallocPointer(1);
            if (FreeType.FT_Get_MM_Var(face, masterAddressBuffer) != FreeType.FT_Err_Ok) {
                return Collections.emptyList();
            }
            final var master = FT_MM_Var.create(Checks.check(masterAddressBuffer.get()));
            final var numAxes = master.num_axis();
            final var axes = new ArrayList<FontVariationAxis>(numAxes);
            for (var i = 0; i < numAxes; i++) {
                final var axis = master.axis().get(i);
                axes.add(new FontVariationAxis(axis.nameString(),
                    f16Dot16ToFP32(axis.minimum()),
                    f16Dot16ToFP32(axis.maximum()),
                    f16Dot16ToFP32(axis.def())));
            }
            FreeType.FT_Done_MM_Var(library, master);
            return axes;
        }
    }

    public static boolean setFontVariationAxis(final long library,
                                               final FT_Face face,
                                               final String name,
                                               final float coord) {
        try (final var stack = MemoryStack.stackPush()) {
            final var faceFlags = face.face_flags();
            if ((faceFlags & FreeType.FT_FACE_FLAG_MULTIPLE_MASTERS) == 0) {
                return false;
            }
            final var masterAddressBuffer = stack.mallocPointer(1);
            if (FreeType.FT_Get_MM_Var(face, masterAddressBuffer) != FreeType.FT_Err_Ok) {
                return false;
            }
            final var master = FT_MM_Var.create(Checks.check(masterAddressBuffer.get()));
            final var numAxes = master.num_axis();
            final var coordsBuffer = stack.callocCLong(numAxes); // Make sure this is zeroed
            var result = false;
            if (FreeType.FT_Get_Var_Design_Coordinates(face, coordsBuffer) == FreeType.FT_Err_Ok) {
                for (var i = 0; i < numAxes; i++) {
                    final var axis = master.axis().get(i);
                    if (!axis.nameString().equals(name)) {
                        continue;
                    }
                    coordsBuffer.put(i, fp32ToF16Dot16(coord));
                    result = true;
                    break;
                }
            }
            if (FreeType.FT_Set_Var_Design_Coordinates(face, coordsBuffer) != FreeType.FT_Err_Ok) {
                result = false;
            }
            FreeType.FT_Done_MM_Var(library, master);
            return result;
        }
    }
}
