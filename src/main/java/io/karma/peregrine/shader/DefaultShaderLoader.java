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

import io.karma.peregrine.Peregrine;
import io.karma.peregrine.util.HashUtils;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL20;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultShaderLoader extends AbstractShaderLoader {
    @Override
    public void save(final Path directory,
                     final ResourceProvider resourceProvider,
                     final ShaderProgram program,
                     final ShaderObject object) {
        try {
            final var fingerprint = getFingerprint(program, object);
            final var file = directory.resolve(String.format("%s.glsl", fingerprint));
            if (Files.exists(file)) {
                Files.delete(file);
            }
            saveText(file, GL20.glGetShaderSource(object.getId()));
            final var fingerprintFile = directory.resolve(String.format("%s.md5", fingerprint));
            if (Files.exists(fingerprintFile)) {
                Files.delete(fingerprintFile);
            }
            saveText(fingerprintFile, HashUtils.toFingerprint(loadSource(resourceProvider, object.getLocation())));
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not save processed shader source", error);
        }
    }

    @Override
    public LoadResult load(final Path directory,
                           final ResourceProvider resourceProvider,
                           final ShaderProgram program,
                           final ShaderObject object) {
        final var fingerprint = getFingerprint(program, object);
        final var file = directory.resolve(String.format("%s.glsl", fingerprint));
        final var fingerprintFile = directory.resolve(String.format("%s.md5", fingerprint));
        final var location = object.getLocation();
        final var source = loadSource(resourceProvider, location);
        if (Files.exists(file)) {
            if (!Files.exists(fingerprintFile)) {
                Peregrine.LOGGER.error("Shader {} missing fingerprint file, aborting", location);
                return new LoadResult(false, false);
            }
            final var previousFingerprint = loadText(fingerprintFile);
            if (Objects.requireNonNull(HashUtils.toFingerprint(source)).equals(previousFingerprint)) {
                Peregrine.LOGGER.debug("Shader cache hit for {}", location);
                GL20.glShaderSource(object.getId(), loadText(file));
                return new LoadResult(true, false);
            }
            else {
                try {
                    Peregrine.LOGGER.debug("Invalidating shader source cache entry {} for object {}",
                        fingerprint,
                        location);
                    if (Files.exists(file)) {
                        Files.delete(file);
                    }
                    if (Files.exists(fingerprintFile)) {
                        Files.delete(fingerprintFile);
                    }
                }
                catch (Throwable error) {
                    Peregrine.LOGGER.error("Could not delete resident shader source cache files", error);
                }
            }
        }
        Peregrine.LOGGER.debug("Shader cache miss for {}", location);
        GL20.glShaderSource(object.getId(),
            object.getPreProcessor().process(source, program, object, loc -> loadSource(resourceProvider, loc)));
        return new LoadResult(true, true);
    }
}
