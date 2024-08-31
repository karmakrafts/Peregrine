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
import org.lwjgl.opengl.ARBGetProgramBinary;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedInputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Alexander Hinze
 * @since 31/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class BinaryShaderLoader extends AbstractShaderLoader {
    @Override
    public void prepareProgram(final ShaderProgram program) {
        ARBGetProgramBinary.glProgramParameteri(program.getId(),
            ARBGetProgramBinary.GL_PROGRAM_BINARY_RETRIEVABLE_HINT,
            GL11.GL_TRUE);
    }

    private static int getProgramSourceHash(final ResourceProvider resourceProvider, final ShaderProgram program) {
        var hash = 0;
        for (final var object : program.getObjects()) {
            final var source = loadSource(resourceProvider, object.getLocation());
            hash = HashUtils.combine(source.hashCode(), hash);
        }
        return hash;
    }

    @Override
    public void saveProgram(final Path directory,
                            final ResourceProvider resourceProvider,
                            final ShaderProgram program) {
        try {
            final var fingerprint = HashUtils.toFingerprint(program.hashCode());
            final var file = directory.resolve(String.format("%s.bin", fingerprint));
            final var fingerprintFile = directory.resolve(String.format("%s.md5", fingerprint));
            if (Files.exists(file)) {
                Files.delete(file);
            }
            if (Files.exists(fingerprintFile)) {
                Files.delete(fingerprintFile);
            }
            saveText(fingerprintFile, HashUtils.toFingerprint(getProgramSourceHash(resourceProvider, program)));

            final var id = program.getId();
            if (GL20.glGetProgrami(id, ARBGetProgramBinary.GL_PROGRAM_BINARY_RETRIEVABLE_HINT) == GL11.GL_FALSE) {
                Peregrine.LOGGER.warn("Could not save shader program binary for program {}", id);
                return;
            }

            final var size = GL20.glGetProgrami(id, ARBGetProgramBinary.GL_PROGRAM_BINARY_LENGTH);
            final var data = MemoryUtil.memAlloc(size);

            try (final var stack = MemoryStack.stackPush()) {
                final var format = stack.mallocInt(1);
                ARBGetProgramBinary.glGetProgramBinary(id, null, format, data);
                if (format.get() != Peregrine.getShaderBinaryFormat()) {
                    Peregrine.LOGGER.warn("Mismatching shader program binary format");
                }
            }

            try (final var channel = Channels.newChannel(Files.newOutputStream(file))) {
                channel.write(data);
            }
            MemoryUtil.memFree(data);
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not save shader program binary", error);
        }

    }

    @Override
    public boolean loadProgram(final Path directory,
                               final ResourceProvider resourceProvider,
                               final ShaderProgram program) {
        try {
            final var fingerprint = HashUtils.toFingerprint(program.hashCode());
            final var file = directory.resolve(String.format("%s.bin", fingerprint));
            final var fingerprintFile = directory.resolve(String.format("%s.md5", fingerprint));
            if (!Files.exists(file) || !Files.exists(fingerprintFile)) {
                Peregrine.LOGGER.debug("Shader binary cache miss for program {}", program);
                return false;
            }
            if (loadText(fingerprintFile).equals(HashUtils.toFingerprint(getProgramSourceHash(resourceProvider,
                program)))) {
                try (final var stream = new BufferedInputStream(Files.newInputStream(file)); final var channel = Channels.newChannel(
                    stream)) {
                    final var size = stream.available();
                    final var data = MemoryUtil.memAlloc(size);
                    channel.read(data);
                    data.flip();
                    ARBGetProgramBinary.glProgramBinary(program.getId(), Peregrine.getShaderBinaryFormat(), data);
                    MemoryUtil.memFree(data);
                    Peregrine.LOGGER.debug("Shader binary cache hit for program {}", program);
                    return true;
                }
            }
            Peregrine.LOGGER.debug("Invalidating shader binary cache entry {} for program {}",
                fingerprint,
                program.getId());
            if (Files.exists(file)) {
                Files.delete(file);
            }
            if (Files.exists(fingerprintFile)) {
                Files.delete(fingerprintFile);
            }
            return false;
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not load shader program binary", error);
            return false;
        }

    }

    @Override
    public LoadResult load(final Path directory,
                           final ResourceProvider resourceProvider,
                           final ShaderProgram program,
                           final ShaderObject object) {
        GL20.glShaderSource(object.getId(), loadAndProcessSource(resourceProvider, program, object));
        return LoadResult.COMPILE;
    }
}
