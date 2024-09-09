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

package io.karma.peregrine.color;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.karma.peregrine.util.RectangleCorner;
import io.karma.peregrine.util.Requires;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class when instantiated represents an RGB color with
 * an alpha component which may be converted into different
 * color spaces or channel orderings by means of swizzling.
 * <p>
 * Also provides static methods for expensive functions to
 * avoid redundant object creations and thus GC pressure.
 *
 * @author Alexander Hinze
 * @since 01/09/2024
 */
public final class Color implements ColorProvider, Comparable<Color> {
    // @formatter:off
    public static final Color NONE       = new Color(0F, 0F, 0F, 0F);
    public static final Color BLACK      = new Color(0F, 0F, 0F);
    public static final Color GREY       = new Color(0.2F, 0.2F, 0.2F);
    public static final Color LIGHT_GREY = new Color(0.4F, 0.4F, 0.4F);
    public static final Color WHITE      = new Color(1F, 1F, 1F);
    public static final Color RED        = new Color(1F, 0F, 0F);
    public static final Color GREEN      = new Color(0F, 1F, 0F);
    public static final Color BLUE       = new Color(0F, 0F, 1F);
    public static final Color LIGHT_BLUE = new Color(0F, 0.4F, 1F);
    public static final Color YELLOW     = new Color(1F, 1F, 0F);
    public static final Color ORANGE     = new Color(1F, 0.6F, 0F);
    public static final Color CYAN       = new Color(0F, 1F, 1F);
    public static final Color MAGENTA    = new Color(1F, 0F, 1F);
    public static final Color PINK       = new Color(1F, 0.7F, 0.7F);

    private static final float[][] RGB_TO_XYZ_COEFFS = {
        {0.4124564F, 0.3575761F, 0.1804375F},
        {0.2126729F, 0.7151522F, 0.0721750F},
        {0.0193339F, 0.1191920F, 0.9503041F}
    };

    private static final float[][] XYZ_TO_RGB_COEFFS = {
        {3.2404542F, -1.5371385F, -0.4985314F},
        {-0.9692660F, 1.8760108F, 0.0415560F},
        {0.0556434F, -0.2040259F, 1.0572252F}
    };

    private static final float[] D65_WHITE_POINT = { 95.047F, 100F, 108.883F };
    // @formatter:on

    @JsonIgnore
    public final float r;
    @JsonIgnore
    public final float g;
    @JsonIgnore
    public final float b;
    @JsonIgnore
    public final float a;

    @JsonIgnore
    public Color() {
        r = g = b = a = 1F;
    }

    @JsonIgnore
    public Color(final float[] values) {
        Requires.that(values.length >= 3 && values.length <= 4, "Array has invalid size");
        r = values[0];
        g = values[1];
        b = values[2];
        a = values.length == 4 ? values[3] : 1F;
    }

    @JsonIgnore
    public Color(final float r, final float g, final float b, final float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @JsonIgnore
    public Color(final float r, final float g, final float b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 1F;
    }

    @JsonIgnore
    public Color(final byte r, final byte g, final byte b, final byte a) {
        this.r = (float) ((r & 0xFF) / 255.0);
        this.g = (float) ((g & 0xFF) / 255.0);
        this.b = (float) ((b & 0xFF) / 255.0);
        this.a = (float) ((a & 0xFF) / 255.0);
    }

    @JsonIgnore
    public Color(final byte r, final byte g, final byte b) {
        this(r, g, b, (byte) 0xFF);
    }

    @JsonIgnore
    public Color(final int r, final int g, final int b) {
        this.r = (float) ((r & 0xFF) / 255.0);
        this.g = (float) ((g & 0xFF) / 255.0);
        this.b = (float) ((b & 0xFF) / 255.0);
        this.a = 0xFF;
    }

    @JsonIgnore
    public Color(final int r, final int g, final int b, final int a) {
        this.r = (float) ((r & 0xFF) / 255.0);
        this.g = (float) ((g & 0xFF) / 255.0);
        this.b = (float) ((b & 0xFF) / 255.0);
        this.a = (float) ((a & 0xFF) / 255.0);
    }

    public static Color fromMapColor(final MapColor color) {
        return unpackARGB(color.col);
    }

    public static Color fromTextColor(final TextColor color) {
        return unpackARGB(color.getValue());
    }

    public static Color unpackRGB(final int value) {
        return new Color((byte) ((value >> 16) & 0xFF), (byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF), 255);
    }

    @JsonCreator
    public static Color unpackRGBA(final @JsonProperty("value") int value) {
        return new Color((byte) ((value >> 24) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) (value & 0xFF));
    }

    public static Color unpackARGB(final int value) {
        return new Color((byte) (value & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) ((value >> 24) & 0xFF));
    }

    private static float applyLinearGamma(final float value) {
        if (value > 0.04045F) {
            return (float) Math.pow((value + 0.055F) / 1.055F, 2.4F);
        }
        return value / 12.92F;
    }

    private static float removeLinearGamma(final float value) {
        if (value <= 0.0031308F) {
            return 12.92F * value;
        }
        return 1.055F * (float) Math.pow(value, 1.0F / 2.4F) - 0.055F;
    }

    public static void transformSRGBToLinearRGB(final float[] values) {
        Requires.that(values.length >= 3, "Array has invalid size");
        values[0] = applyLinearGamma(values[0]);
        values[1] = applyLinearGamma(values[1]);
        values[2] = applyLinearGamma(values[2]);
    }

    public static void transformLinearRGBToSRGB(final float[] values) {
        Requires.that(values.length >= 3, "Array has invalid size");
        values[0] = removeLinearGamma(values[0]);
        values[1] = removeLinearGamma(values[1]);
        values[2] = removeLinearGamma(values[2]);
    }

    // https://en.wikipedia.org/wiki/CIE_1931_color_space
    public static void transformRGBToXYZ(final float[] values) {
        Requires.that(values.length >= 3, "Array has invalid size");
        values[0] = RGB_TO_XYZ_COEFFS[0][0] * values[0] + RGB_TO_XYZ_COEFFS[0][1] * values[1] + RGB_TO_XYZ_COEFFS[0][2] * values[2];
        values[1] = RGB_TO_XYZ_COEFFS[1][0] * values[0] + RGB_TO_XYZ_COEFFS[1][1] * values[1] + RGB_TO_XYZ_COEFFS[1][2] * values[2];
        values[2] = RGB_TO_XYZ_COEFFS[2][0] * values[0] + RGB_TO_XYZ_COEFFS[2][1] * values[1] + RGB_TO_XYZ_COEFFS[2][2] * values[2];
    }

    public static void transformXYZToRGB(final float[] values) {
        Requires.that(values.length >= 3, "Array has invalid size");
        values[0] = XYZ_TO_RGB_COEFFS[0][0] * values[0] + XYZ_TO_RGB_COEFFS[0][1] * values[1] + XYZ_TO_RGB_COEFFS[0][2] * values[2];
        values[1] = XYZ_TO_RGB_COEFFS[1][0] * values[0] + XYZ_TO_RGB_COEFFS[1][1] * values[1] + XYZ_TO_RGB_COEFFS[1][2] * values[2];
        values[2] = XYZ_TO_RGB_COEFFS[2][0] * values[0] + XYZ_TO_RGB_COEFFS[2][1] * values[1] + XYZ_TO_RGB_COEFFS[2][2] * values[2];
    }

    public static Color fromXYZ(final float[] values) {
        transformXYZToRGB(values);
        return new Color(values);
    }

    public static Color fromXYZ(final float x, final float y, final float z) {
        return fromXYZ(new float[]{x, y, z});
    }

    // https://en.wikipedia.org/wiki/CIELAB_color_space
    private static float transformToLAB(final float r) {
        return (r > 0.008856F) ? (float) Math.pow(r, 1F / 3F) : (7.787F * r + 16F / 116F);
    }

    public static void transformRGBToLAB(final float[] values) {
        Requires.that(values.length >= 3, "Array has invalid size");
        transformRGBToXYZ(values);
        final var fy = transformToLAB(values[1] / D65_WHITE_POINT[1]);
        values[0] = (116F * fy) - 16F;
        values[1] = 500.0F * (transformToLAB(values[0] / D65_WHITE_POINT[0]) - fy);
        values[2] = 200.0F * (fy - transformToLAB(values[2] / D65_WHITE_POINT[2]));
    }

    public static void transformLABToRGB(final float[] values) {
        Requires.that(values.length >= 3, "Array has invalid size");
        final var l = values[0];
        final var fy = (l + 16F) / 116F;
        final var fx = values[1] / 500F + fy;
        final var fz = fy - values[2] / 200F;
        final var xp = (float) Math.pow(fx, 3F);
        final var zp = (float) Math.pow(fz, 3F);
        values[0] = ((xp > 0.008856F) ? xp : (116F * fx - 16F) / 903.3F) * D65_WHITE_POINT[0];
        values[1] = ((l > 8F) ? (float) Math.pow((l + 16F) / 116F, 3F) : l / 903.3F) * D65_WHITE_POINT[1];
        values[2] = ((zp > 0.008856F) ? zp : (116F * fz - 16F) / 903.3F) * D65_WHITE_POINT[2];
        transformXYZToRGB(values);
    }

    public static Color fromLAB(final float[] values) {
        transformLABToRGB(values);
        return new Color(values);
    }

    public static Color fromLAB(final float l, final float a, final float b) {
        return fromLAB(new float[]{l, a, b});
    }

    // https://en.wikipedia.org/wiki/HCL_color_space
    public static void transformRGBToLCH(final float[] values) {
        Requires.that(values.length >= 3, "Array has invalid size");
        transformRGBToLAB(values);
        final var a = values[1];
        final var b = values[2];
        values[1] = (float) Math.sqrt(a * a + b * b);
        values[2] = (float) Math.toDegrees(Math.atan2(b, a));
    }

    @JsonIgnore
    public void storeRGBInto(final float[] values) {
        Requires.that(values.length >= 3, "Array has invalid size");
        values[0] = r;
        values[1] = g;
        values[2] = b;
        if (values.length == 4) {
            values[3] = a;
        }
    }

    @JsonIgnore
    public void toXYZ(final float[] values) {
        storeRGBInto(values);
        transformRGBToXYZ(values);
    }

    @JsonIgnore
    public float[] toXYZ() {
        final var values = new float[3];
        toXYZ(values);
        return values;
    }

    @JsonIgnore
    public void toLAB(final float[] values) {
        storeRGBInto(values);
        transformRGBToLAB(values);
    }

    @JsonIgnore
    public float[] toLAB() {
        final var values = new float[3];
        toLAB(values);
        return values;
    }

    // TODO: implement reverse transform

    @JsonIgnore
    public void toLCH(final float[] values) {
        storeRGBInto(values);
        transformRGBToLCH(values);
    }

    @JsonIgnore
    public float[] toLCH() {
        final var values = new float[3];
        toLCH(values);
        return values;
    }

    @JsonIgnore
    public float getLuminance() {
        return Mth.clamp(r * 0.21F + g * 0.71F + b * 0.07F, 0F, 1F);
    }

    @JsonIgnore
    public int packRGB() {
        return (int) (r * 255F) << 16 | (int) (g * 255F) << 8 | (int) (b * 255F);
    }

    @JsonGetter("value")
    public int packRGBA() {
        return (int) (r * 255F) << 24 | (int) (g * 255F) << 16 | (int) (b * 255F) << 8 | (int) (a * 255F);
    }

    @JsonIgnore
    public int packARGB() {
        return (int) (a * 255F) << 24 | (int) (r * 255F) << 16 | (int) (g * 255F) << 8 | (int) (b * 255F);
    }

    @Override
    public int getColor(final RectangleCorner corner) {
        return packARGB();
    }

    @Override
    public int compareTo(final @NotNull Color o) {
        return Float.compare(getLuminance(), o.getLuminance());
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b, a);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Color color)) {
            return false;
        }
        return r == color.r && g == color.g && b == color.b && a == color.a;
    }

    @Override
    public String toString() {
        return String.format("Color[%.2f,%.2f,%.2f,%.2f]", r, g, b, a);
    }
}
