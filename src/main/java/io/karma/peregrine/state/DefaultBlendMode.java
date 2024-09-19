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
import io.karma.peregrine.api.state.BlendMode;
import io.karma.peregrine.api.util.HashUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Alexander Hinze
 * @since 07/09/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultBlendMode implements BlendMode {
    private final SourceFactor colorSourceFactor;
    private final SourceFactor alphaSourceFactor;
    private final DestFactor colorDestFactor;
    private final DestFactor alphaDestFactor;

    public DefaultBlendMode(final SourceFactor colorSourceFactor,
                            final SourceFactor alphaSourceFactor,
                            final DestFactor colorDestFactor,
                            final DestFactor alphaDestFactor) {
        this.colorSourceFactor = colorSourceFactor;
        this.alphaSourceFactor = alphaSourceFactor;
        this.colorDestFactor = colorDestFactor;
        this.alphaDestFactor = alphaDestFactor;
    }

    @Override
    public SourceFactor getSourceFactor() {
        return alphaSourceFactor;
    }

    @Override
    public DestFactor getDestFactor() {
        return alphaDestFactor;
    }

    @Override
    public SourceFactor getColorSourceFactor() {
        return colorSourceFactor;
    }

    @Override
    public DestFactor getColorDestFactor() {
        return colorDestFactor;
    }

    @Override
    public int hashCode() {
        return HashUtils.combineMany(colorSourceFactor.ordinal(),
            alphaSourceFactor.ordinal(),
            colorDestFactor.ordinal(),
            alphaDestFactor.ordinal());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlendMode other)) {
            return false;
        }
        return colorSourceFactor == other.getSourceFactor() && alphaSourceFactor == other.getSourceFactor() && colorDestFactor == other.getDestFactor() && alphaDestFactor == other.getDestFactor();
    }

    @Override
    public String toString() {
        return String.format(
            "DefaultBlendMode[colorSourceFactor=%s,alphaSourceFactor=%s,colorDestFactor=%s,alphaDestFactor=%s]",
            colorSourceFactor,
            alphaSourceFactor,
            colorDestFactor,
            alphaDestFactor);
    }
}
