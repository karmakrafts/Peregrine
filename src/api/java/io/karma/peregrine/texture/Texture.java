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

package io.karma.peregrine.texture;

import io.karma.peregrine.Peregrine;
import io.karma.peregrine.dispose.Disposable;
import io.karma.peregrine.reload.Reloadable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public interface Texture extends Reloadable, Disposable {
    static Texture create(final ResourceLocation location) {
        return Peregrine.getTextureFactories().get(location);
    }

    int getId();

    void bind();

    void unbind();
}
