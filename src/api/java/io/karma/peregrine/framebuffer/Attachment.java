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

import io.karma.peregrine.dispose.Disposable;
import io.karma.peregrine.texture.DynamicTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Represents a framebuffer attachment.
 * This can be a color, depth, stencil or combined depth-stencil attachment.
 * See {@link AttachmentType}.
 *
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface Attachment extends Disposable {
    /**
     * Retrieves the type of this framebuffer attachment.
     *
     * @return the type of this framebuffer attachment.
     */
    AttachmentType getType();

    /**
     * Retrieves the underlying texture of this framebuffer attachment.
     *
     * @return the {@link DynamicTexture} backing this framebuffer attachment.
     * <b>May only be used as a read-only texture!</b>
     */
    DynamicTexture getTexture();
}
