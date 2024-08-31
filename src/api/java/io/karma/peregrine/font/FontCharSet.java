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

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntIntPair;

/**
 * Describes a set of characters supported
 * by a given font.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
public interface FontCharSet {
    /**
     * Retrieves all characters ranges supported by
     * a given font.
     *
     * @return all character ranges supported by a given font.
     */
    IntIntPair[] getRanges();

    /**
     * Determines the total number of characters supported
     * by a given font, contained within this set.
     *
     * @return the total number of characters supported by a given font.
     */
    default int getCharCount() {
        var charCount = 0;
        for (final var range : getRanges()) {
            charCount += range.rightInt() - range.leftInt();
        }
        return charCount;
    }

    /**
     * Creates an array which contains all expanded
     * character ranges contained within this set.
     *
     * @return a new array which contains all expanded character ranges contained within this set.
     */
    default char[] toArray() {
        final var chars = new CharArrayList(getCharCount());
        for (final var range : getRanges()) {
            for (var i = range.leftInt(); i <= range.rightInt(); i++) {
                chars.add((char) i);
            }
        }
        return chars.toCharArray();
    }

    /**
     * Creates a new hash set which contains all expanded
     * character ranges contained within this set.
     *
     * @return a new hash set which contains all expanded character ranges contained within this set.
     */
    default CharOpenHashSet toSet() {
        final var set = new CharOpenHashSet(getCharCount());
        for (final var range : getRanges()) {
            for (var i = range.leftInt(); i <= range.rightInt(); i++) {
                set.add((char) i);
            }
        }
        return set;
    }
}
