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

import com.mojang.blaze3d.vertex.VertexBuffer;
import io.karma.peregrine.hooks.PeregrineShader;
import io.karma.peregrine.hooks.PeregrineVertexBuffer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin implements PeregrineVertexBuffer {
    @Shadow
    public abstract void drawWithShader(final Matrix4f modelViewMatrix,
                                        final Matrix4f projectionMatrix,
                                        final ShaderInstance shader);

    @Shadow
    public abstract void draw();

    @Override
    public void peregrine$drawWithShader(final Matrix4f modelViewMatrix,
                                         final Matrix4f projectionMatrix,
                                         final PeregrineShader shader) {
        if (shader instanceof ShaderInstance instance) {
            drawWithShader(modelViewMatrix, projectionMatrix, instance);
            return;
        }
        shader.apply();
        draw();
        shader.clear();
    }
}
