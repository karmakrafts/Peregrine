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

import io.karma.peregrine.api.target.RenderTarget;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * @author Alexander Hinze
 * @since 01/09/2024
 */
@OnlyIn(Dist.CLIENT)
public final class ProxyRenderTarget implements RenderTarget {
    private final int framebufferId;
    private final ProxyOutputState stateProxy = new ProxyOutputState(this);
    private int previousDrawFramebufferId;
    private int previousReadFramebufferId;

    public ProxyRenderTarget(final int framebufferId) {
        this.framebufferId = framebufferId;
    }

    @Override
    public OutputStateShard asStateShard() {
        return stateProxy;
    }

    @Override
    public void bind() {
        previousDrawFramebufferId = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        previousReadFramebufferId = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
    }

    @Override
    public void unbind() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDrawFramebufferId);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousReadFramebufferId);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(framebufferId);
    }

    @Override
    public String toString() {
        return String.format("ProxyRenderTarget[framebufferId=%d]", framebufferId);
    }
}
