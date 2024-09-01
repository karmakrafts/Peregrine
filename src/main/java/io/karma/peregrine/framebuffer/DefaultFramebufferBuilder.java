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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumMap;
import java.util.function.Consumer;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultFramebufferBuilder implements FramebufferBuilder {
    private static final Consumer<Framebuffer> IDENTITY_CALLBACK = buffer -> {
    };
    private final EnumMap<AttachmentType, Attachment> attachments = new EnumMap<>(AttachmentType.class);
    private int width;
    private int height;
    private Consumer<Framebuffer> bindCallback = IDENTITY_CALLBACK;
    private Consumer<Framebuffer> unbindCallback = IDENTITY_CALLBACK;

    public static Framebuffer build(final Consumer<FramebufferBuilder> callback) {
        final var builder = new DefaultFramebufferBuilder();
        callback.accept(builder);
        return builder.build();
    }

    @Override
    public FramebufferBuilder width(final int width) {
        this.width = width;
        return this;
    }

    @Override
    public FramebufferBuilder height(final int height) {
        this.height = height;
        return this;
    }

    @Override
    public FramebufferBuilder attachment(final Consumer<AttachmentBuilder> callback) {
        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("Width or height of attachment cannot be 0");
        }
        final var builder = new DefaultAttachmentBuilder(width, height);
        callback.accept(builder);
        final var attachment = builder.build();
        final var type = attachment.getType();
        if (attachments.containsKey(type)) {
            throw new IllegalArgumentException("Framebuffer already contains attachment of same type");
        }
        attachments.put(type, attachment);
        return this;
    }

    @Override
    public FramebufferBuilder onBind(final Consumer<Framebuffer> callback) {
        if (bindCallback == IDENTITY_CALLBACK) {
            bindCallback = callback;
            return this;
        }
        bindCallback = bindCallback.andThen(callback);
        return this;
    }

    @Override
    public FramebufferBuilder onUnbind(final Consumer<Framebuffer> callback) {
        if (unbindCallback == IDENTITY_CALLBACK) {
            unbindCallback = callback;
            return this;
        }
        unbindCallback = unbindCallback.andThen(callback);
        return this;
    }

    private DefaultFramebuffer build() {
        return new DefaultFramebuffer(width, height, attachments);
    }
}
