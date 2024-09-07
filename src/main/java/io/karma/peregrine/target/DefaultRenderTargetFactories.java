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

package io.karma.peregrine.target;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;

/**
 * @author Alexander Hinze
 * @since 01/09/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultRenderTargetFactories implements RenderTargetFactories {
    private final Int2ObjectOpenHashMap<ProxyRenderTarget> proxyTargets = new Int2ObjectOpenHashMap<>();
    private final HashMap<OutputStateShard, OutputStateRenderTarget> stateTargets = new HashMap<>();
    private final ProxyRenderTarget defaultTarget = new ProxyRenderTarget(0);
    private final OutputStateRenderTarget mainTarget = new OutputStateRenderTarget(RenderType.MAIN_TARGET);
    private final OutputStateRenderTarget outlineTarget = new OutputStateRenderTarget(RenderType.OUTLINE_TARGET);
    private final OutputStateRenderTarget translucentTarget = new OutputStateRenderTarget(RenderType.TRANSLUCENT_TARGET);
    private final OutputStateRenderTarget particleTarget = new OutputStateRenderTarget(RenderType.PARTICLES_TARGET);
    private final OutputStateRenderTarget weatherTarget = new OutputStateRenderTarget(RenderType.WEATHER_TARGET);
    private final OutputStateRenderTarget cloudTarget = new OutputStateRenderTarget(RenderType.CLOUDS_TARGET);
    private final OutputStateRenderTarget itemEntityTarget = new OutputStateRenderTarget(RenderType.ITEM_ENTITY_TARGET);

    @Override
    public RenderTarget get(final OutputStateShard outputState) {
        return stateTargets.computeIfAbsent(outputState, OutputStateRenderTarget::new);
    }

    @Override
    public RenderTarget get(final int framebufferId) {
        return proxyTargets.computeIfAbsent(framebufferId, ProxyRenderTarget::new);
    }

    @Override
    public RenderTarget getDefault() {
        return defaultTarget;
    }

    @Override
    public RenderTarget getMainTarget() {
        return mainTarget;
    }

    @Override
    public RenderTarget getOutlineTarget() {
        return outlineTarget;
    }

    @Override
    public RenderTarget getTranslucentTarget() {
        return translucentTarget;
    }

    @Override
    public RenderTarget getParticleTarget() {
        return particleTarget;
    }

    @Override
    public RenderTarget getWeatherTarget() {
        return weatherTarget;
    }

    @Override
    public RenderTarget getCloudTarget() {
        return cloudTarget;
    }

    @Override
    public RenderTarget getItemEntityTarget() {
        return itemEntityTarget;
    }
}
