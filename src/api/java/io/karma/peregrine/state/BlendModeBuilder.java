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
public interface BlendModeBuilder {
    BlendModeBuilder colorSource(final SourceFactor factor);

    BlendModeBuilder alphaSource(final SourceFactor factor);

    BlendModeBuilder colorDest(final DestFactor factor);

    BlendModeBuilder alphaDest(final DestFactor factor);

    default BlendModeBuilder source(final SourceFactor factor) {
        return colorSource(factor).alphaSource(factor);
    }

    default BlendModeBuilder dest(final DestFactor factor) {
        return colorDest(factor).alphaDest(factor);
    }
}
