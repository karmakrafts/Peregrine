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

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
public interface Font {
    FontFamily getFamily();

    FontCharSet getSupportedChars();

    ResourceLocation getLocation();

    FontVariant getDefaultVariant();

    Object2FloatMap<String> getVariationAxes();

    float getVariationAxis(final String name);

    default FontVariant asVariant() {
        return this instanceof FontVariant variant ? variant : getDefaultVariant();
    }
}
