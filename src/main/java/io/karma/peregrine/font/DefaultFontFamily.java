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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.api.font.DistanceFieldType;
import io.karma.peregrine.api.font.FontFamily;
import io.karma.peregrine.api.font.FontStyle;
import io.karma.peregrine.api.font.FontVariant;
import io.karma.peregrine.api.reload.ReloadPriority;
import io.karma.peregrine.api.reload.Reloadable;
import io.karma.peregrine.api.util.Requires;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@ReloadPriority(-50) // Right before FontRenderer
public final class DefaultFontFamily implements FontFamily, Reloadable {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ResourceLocation name;
    private final Set<FontStyle> styles = Collections.synchronizedSet(EnumSet.noneOf(FontStyle.class));
    private final Map<FontStyle, DefaultFont> fonts = Collections.synchronizedMap(new EnumMap<>(FontStyle.class));
    private Config config;

    public DefaultFontFamily(final ResourceLocation name) {
        this.name = name;
        PeregrineMod.RELOAD_HANDLER.register(this);
    }

    private static <T> @Nullable T read(final Resource resource, final Class<T> type) {
        try {
            try (final var stream = resource.open()) {
                return OBJECT_MAPPER.readValue(stream, type);
            }
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not read JSON resource", error);
            return null;
        }
    }

    @Override
    public synchronized void prepare(final ResourceProvider resourceProvider) {
        try {
            final var configPath = String.format("fonts/%s.json", name.getPath());
            final var configLocation = new ResourceLocation(name.getNamespace(), configPath);
            Peregrine.LOGGER.debug("Loading font family from {}", configLocation);
            config = Objects.requireNonNull(read(resourceProvider.getResourceOrThrow(configLocation), Config.class));
            Requires.that(config.version >= Config.VERSION,
                () -> String.format("Invalid font config version %d, expected at least %d",
                    config.version,
                    Config.VERSION));
            styles.clear();
            styles.addAll(config.variants.keySet());
            for (final var style : styles) {
                getFont(style, FontVariant.DEFAULT_SIZE);
            }
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not read font config {}: {}", name, error);
        }
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
    }

    @Override
    public DistanceFieldType getDistanceFieldType() {
        return config.sdfType;
    }

    @Override
    public float getDistanceFieldRange() {
        return config.sdfRange;
    }

    @Override
    public int getGlyphSpriteBorder() {
        return config.glyphSpriteBorder;
    }

    @Override
    public int getGlyphSpriteSize() {
        return config.glyphSpriteSize;
    }

    @Override
    public ResourceLocation getName() {
        return name;
    }

    @Override
    public synchronized String getDisplayName() {
        return config.name;
    }

    @Override
    public synchronized Set<FontStyle> getStyles() {
        return styles;
    }

    @Override
    public synchronized FontVariant getFont(final FontStyle style, final float size) {
        Requires.that(size > 0F, "Size must be greater than or equal to zero");
        return new DefaultFontVariant(fonts.computeIfAbsent(style, s -> {
            final var variant = config.variants.get(s);
            final var locationString = variant.location;
            final var location = ResourceLocation.tryParse(locationString);
            Requires.that(location != null, () -> String.format("Malformed font location: %s", locationString));
            final var font = new DefaultFont(this, config.supportedCharSet, location);
            font.setVariationAxes(variant.variationAxes);
            return font;
        }), style, size);
    }

    @Override
    public synchronized FontVariant getFont(final FontStyle style,
                                            final float size,
                                            final Object2FloatMap<String> variationAxes) {
        Requires.that(size > 0F, "Size must be greater than zero");
        return new DefaultFontVariant(fonts.computeIfAbsent(style, s -> {
            final var variant = config.variants.get(s);
            final var locationString = variant.location;
            final var location = ResourceLocation.tryParse(locationString);
            Requires.that(location != null, () -> String.format("Malformed font location: %s", locationString));
            final var font = new DefaultFont(this, config.supportedCharSet, location);
            font.setVariationAxes(variationAxes);
            return font;
        }), style, size); // TODO: finish implementing this
    }

    @Override
    public String toString() {
        return String.format("DefaultFontFamily[name=%s,styles=%s]", name, styles);
    }

    public static final class Config {
        @JsonIgnore
        public static final int VERSION = 1;
        @JsonProperty
        public int version = VERSION;
        @JsonProperty
        public String name;
        @JsonProperty("sdf_range")
        public float sdfRange = 4F;
        @JsonProperty("sdf_type")
        public DistanceFieldType sdfType = DistanceFieldType.MSDF;
        @JsonProperty("glyph_sprite_size")
        public int glyphSpriteSize = 32;
        @JsonProperty("glyph_sprite_border")
        public int glyphSpriteBorder = 2;
        @JsonProperty("supported_char_set")
        public DefaultCharSet supportedCharSet = DefaultCharSet.EXTENDED_ASCII;
        @JsonProperty
        public HashMap<FontStyle, Variant> variants = new HashMap<>();

        public static final class Variant {
            @JsonProperty
            public String location;
            @JsonProperty("variation_axes")
            public Object2FloatOpenHashMap<String> variationAxes = new Object2FloatOpenHashMap<>();
        }
    }
}