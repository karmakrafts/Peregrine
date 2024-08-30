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

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
public interface FontSet {
    static FontSet of(final Font heading1, final Font heading2, final Font heading3, final Font text) {
        return new DefaultFontSet(heading1, heading2, heading3, text);
    }

    static FontSet of(final Font headings, final Font text) {
        return new DefaultFontSet(headings, headings, headings, text);
    }

    static FontSet of(final Font font) {
        return new DefaultFontSet(font, font, font, font);
    }

    Font getHeading1();

    Font getHeading2();

    Font getHeading3();

    Font getText();
}
