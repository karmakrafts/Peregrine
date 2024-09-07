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

import io.karma.peregrine.color.ColorProvider;
import io.karma.peregrine.target.RenderTarget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.IntFunction;

/**
 * @author Alexander Hinze
 * @since 01/09/2024
 */
@OnlyIn(Dist.CLIENT)
public interface FontRenderer {
    RenderTarget getRenderTarget();

    int getLineHeight(final Font font);

    FontTexture getFontTexture(final Font font);

    int getStringWidth(final Font font, final CharSequence s);

    int render(final int x, final int y, final char c, final Font font, final ColorProvider color);

    int render(final int x, final int y, final CharSequence text, final Font font, final ColorProvider color);

    int render(final int x, final int y, final char c, final Font font, final IntFunction<ColorProvider> color);

    int render(final int x,
               final int y,
               final CharSequence text,
               final Font font,
               final IntFunction<ColorProvider> color);
}
