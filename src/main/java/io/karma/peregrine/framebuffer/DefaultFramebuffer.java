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

package io.karma.peregrine.framebuffer;

import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.api.framebuffer.Attachment;
import io.karma.peregrine.api.framebuffer.AttachmentType;
import io.karma.peregrine.api.framebuffer.Framebuffer;
import io.karma.peregrine.api.reload.Reloadable;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultFramebuffer implements Framebuffer, Reloadable {
    private final EnumMap<AttachmentType, Attachment> attachments;
    private final Consumer<Framebuffer> bindCallback;
    private final Consumer<Framebuffer> unbindCallback;
    private final OutputStateShard outputState;
    private int id;
    private int previousDrawId = INVALID_ID;
    private int previousReadId = INVALID_ID;
    private int previousTexture;
    private int width;
    private int height;
    private boolean isInitialized;

    public DefaultFramebuffer(final int width,
                              final int height,
                              final EnumMap<AttachmentType, Attachment> attachments,
                              final Consumer<Framebuffer> bindCallback,
                              final Consumer<Framebuffer> unbindCallback) {
        id = GL30.glGenFramebuffers();
        this.attachments = attachments;
        this.bindCallback = bindCallback;
        this.unbindCallback = unbindCallback;
        this.width = width;
        this.height = height;
        outputState = new OutputStateShard(toString(), this::bind, this::unbind);
        PeregrineMod.DISPOSE_HANDLER.register(this);
    }

    @Override
    public OutputStateShard asStateShard() {
        return outputState;
    }

    @Override
    public @Nullable Attachment getAttachment(final AttachmentType attachment) {
        return attachments.get(attachment);
    }

    @Override
    public void dispose() {
        if (id == INVALID_ID) {
            return;
        }
        for (final var attachment : attachments.values()) {
            attachment.dispose();
        }
        GL30.glDeleteFramebuffers(id);
        id = INVALID_ID;
    }

    @Override
    public void resize(final int width, final int height) {
        final var attachments = this.attachments.values();
        // Resize attachments
        for (final var attachment : attachments) {
            final var texture = attachment.getTexture();
            texture.resize(width, height);
        }
        // Bind attachments to framebuffer object
        bind();
        for (final var attachment : attachments) {
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,
                attachment.getType().getGLType(),
                GL11.GL_TEXTURE_2D,
                attachment.getTexture().getId(),
                0);
        }
        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            Peregrine.LOGGER.error("Could not create framebuffer");
        }
        unbind();
        // Update internal size
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean hasAttachment(final AttachmentType attachment) {
        return attachments.containsKey(attachment);
    }

    @Override
    public void bind() {
        if (previousDrawId != INVALID_ID) {
            return;
        }
        previousDrawId = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        previousReadId = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
        bindCallback.accept(this);
    }

    @Override
    public void unbind() {
        if (previousDrawId == INVALID_ID) {
            return;
        }
        unbindCallback.accept(this);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDrawId);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousReadId);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
        previousDrawId = INVALID_ID;
    }

    @Override
    public void clear(final float r, final float g, final float b, final float a) {
        GL11.glClearColor(r, g, b, a);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public List<Attachment> getAttachments() {
        return List.copyOf(attachments.values());
    }

    @Override
    public void reload(final ResourceProvider resourceProvider) {
        if (!isInitialized) {
            resize(width, height);
            isInitialized = true;
        }
    }

    @Override
    public String toString() {
        return String.format("DefaultFramebuffer[id=%d,attachments=%s]", id, attachments.values());
    }
}
