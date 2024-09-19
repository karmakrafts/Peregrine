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

package io.karma.peregrine.mixin;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormatElement.Type;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage;
import io.karma.peregrine.api.util.HashUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@Mixin(VertexFormatElement.class)
public final class VertexFormatElementMixin {
    @Shadow
    @Final
    private Type type;
    @Shadow
    @Final
    private Usage usage;
    @Shadow
    @Final
    private int index;
    @Shadow
    @Final
    private int count;

    // Make the hashCode of VFEs reproducible, so we can use it for FS caching
    @Inject(method = "hashCode", at = @At("HEAD"), cancellable = true)
    private void onHashCode(final CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(HashUtils.combineMany(type.ordinal(), usage.ordinal(), index, count));
        cir.cancel();
    }
}
