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

package io.karma.peregrine.state;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

/**
 * @author Alexander Hinze
 * @since 07/09/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultBlendModeBuilder implements BlendModeBuilder {
    private SourceFactor colorSourceFactor;
    private SourceFactor alphaSourceFactor;
    private DestFactor colorDestFactor;
    private DestFactor alphaDestFactor;

    public static BlendMode build(final Consumer<BlendModeBuilder> callback) {
        final var builder = new DefaultBlendModeBuilder();
        callback.accept(builder);
        return builder.build();
    }

    DefaultBlendMode build() {
        Preconditions.checkArgument(colorSourceFactor != null, "Color source factor must be specified");
        Preconditions.checkArgument(alphaSourceFactor != null, "Alpha source factor must be specified");
        Preconditions.checkArgument(colorDestFactor != null, "Color dest factor must be specified");
        Preconditions.checkArgument(alphaDestFactor != null, "Alpha dest factor must be specified");
        return new DefaultBlendMode(colorSourceFactor, alphaSourceFactor, colorDestFactor, alphaDestFactor);
    }

    @Override
    public BlendModeBuilder colorSource(final SourceFactor factor) {
        colorSourceFactor = factor;
        return this;
    }

    @Override
    public BlendModeBuilder alphaSource(final SourceFactor factor) {
        alphaSourceFactor = factor;
        return this;
    }

    @Override
    public BlendModeBuilder colorDest(final DestFactor factor) {
        colorDestFactor = factor;
        return this;
    }

    @Override
    public BlendModeBuilder alphaDest(final DestFactor factor) {
        alphaDestFactor = factor;
        return this;
    }
}
