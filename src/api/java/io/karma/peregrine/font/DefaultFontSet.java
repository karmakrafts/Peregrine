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
public final class DefaultFontSet implements FontSet {
    private final Font heading1;
    private final Font heading2;
    private final Font heading3;
    private final Font text;

    DefaultFontSet(final Font heading1, final Font heading2, final Font heading3, final Font text) {
        this.heading1 = heading1;
        this.heading2 = heading2;
        this.heading3 = heading3;
        this.text = text;
    }

    @Override
    public Font getHeading1() {
        return heading1;
    }

    @Override
    public Font getHeading2() {
        return heading2;
    }

    @Override
    public Font getHeading3() {
        return heading3;
    }

    @Override
    public Font getText() {
        return text;
    }
}
