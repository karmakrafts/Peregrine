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

import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * A type alias for a function which maps a {@link ResourceLocation} to a {@link FontFamily}.
 * Can be used for creating new registry entries.
 *
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@FunctionalInterface
public interface FontFamilyFactory extends Function<ResourceLocation, FontFamily> {
}
