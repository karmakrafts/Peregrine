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

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Alexander Hinze
 * @since 07/09/2024
 */
@OnlyIn(Dist.CLIENT)
public enum Transparency implements BlendMode {
    // @formatter:off
    NONE        (SourceFactor.ONE, DestFactor.ONE),
    TRANSPARENCY(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
    // @formatter:on

    private final SourceFactor sourceFactor;
    private final DestFactor destFactor;

    Transparency(final SourceFactor sourceFactor, final DestFactor destFactor) {
        this.sourceFactor = sourceFactor;
        this.destFactor = destFactor;
    }

    @Override
    public SourceFactor getSourceFactor() {
        return sourceFactor;
    }

    @Override
    public DestFactor getDestFactor() {
        return destFactor;
    }

    @Override
    public boolean usesBlending() {
        return this != NONE;
    }
}
