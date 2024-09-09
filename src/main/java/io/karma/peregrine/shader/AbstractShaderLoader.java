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

package io.karma.peregrine.shader;

import io.karma.peregrine.api.Peregrine;
import io.karma.peregrine.api.shader.ShaderLoader;
import io.karma.peregrine.api.shader.ShaderObject;
import io.karma.peregrine.api.shader.ShaderProgram;
import io.karma.peregrine.api.util.HashUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public abstract class AbstractShaderLoader implements ShaderLoader {
    protected static String getFingerprint(final ShaderProgram program, final ShaderObject object) {
        return HashUtils.toFingerprint(program.hashCode(), object.hashCode());
    }

    protected static String loadSource(final ResourceProvider resourceProvider, final ResourceLocation location) {
        try (final var reader = resourceProvider.openAsReader(location)) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not load shader source", error);
            return "";
        }
    }

    protected static String loadAndProcessSource(final ResourceProvider resourceProvider,
                                                 final ShaderProgram program,
                                                 final ShaderObject object) {
        return object.getPreProcessor().process(loadSource(resourceProvider, object.getLocation()),
            program,
            object,
            location -> loadSource(resourceProvider, location));
    }

    protected static String loadText(final Path path) {
        try (final var reader = Files.newBufferedReader(path)) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not load file", error);
            return "";
        }
    }

    protected static void saveText(final Path path, final String text) {
        try {
            Files.deleteIfExists(path);
            try (final var writer = Files.newBufferedWriter(path)) {
                writer.write(text);
            }
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not save file", error);
        }
    }
}
