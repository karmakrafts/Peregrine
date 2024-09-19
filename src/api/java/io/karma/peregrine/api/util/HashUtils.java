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

package io.karma.peregrine.api.util;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Map;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
public final class HashUtils {
    // @formatter:off
    private HashUtils() {}
    // @formatter:on

    public static int combine(final int hash1, final int hash2) {
        return 31 * hash1 + hash2;
    }

    public static int combineMany(final int... hashes) {
        var hash = hashes[0];
        for (var i = 0; i < hashes.length - 1; i++) {
            hash = combine(hash, hashes[i + 1]);
        }
        return hash;
    }

    public static int hash(final Collection<?> objects) {
        if (objects.isEmpty()) {
            return objects.hashCode();
        }
        final var indexedObjects = objects.toArray(Object[]::new);
        var hash = indexedObjects[0].hashCode();
        for (var i = 0; i < indexedObjects.length - 1; i++) {
            hash = combine(hash, indexedObjects[i + 1].hashCode());
        }
        return hash;
    }

    public static int hash(final Map<?, ?> map) {
        return combine(hash(map.keySet()), hash(map.values()));
    }

    public static @Nullable String toFingerprint(final int hash) {
        try {
            final var data = new byte[4];
            data[0] = (byte) ((hash >> 24) & 0xFF);
            data[1] = (byte) ((hash >> 16) & 0xFF);
            data[2] = (byte) ((hash >> 8) & 0xFF);
            data[3] = (byte) (hash & 0xFF);
            final var digest = MessageDigest.getInstance("MD5");
            digest.update(data);
            final var fingerprintData = digest.digest();
            final var builder = new StringBuilder();
            for (final var b : fingerprintData) {
                builder.append(String.format("%02X", b));
            }
            return builder.toString();
        }
        catch (Throwable error) {
            return null;
        }
    }

    public static @Nullable String toFingerprint(final long hash) {
        try {
            final var data = new byte[8];
            data[0] = (byte) ((hash >> 56) & 0xFF);
            data[1] = (byte) ((hash >> 48) & 0xFF);
            data[2] = (byte) ((hash >> 40) & 0xFF);
            data[3] = (byte) ((hash >> 32) & 0xFF);
            data[4] = (byte) ((hash >> 24) & 0xFF);
            data[5] = (byte) ((hash >> 16) & 0xFF);
            data[6] = (byte) ((hash >> 8) & 0xFF);
            data[7] = (byte) (hash & 0xFF);
            final var digest = MessageDigest.getInstance("MD5");
            digest.update(data);
            final var fingerprintData = digest.digest();
            final var builder = new StringBuilder();
            for (final var b : fingerprintData) {
                builder.append(String.format("%02X", b));
            }
            return builder.toString();
        }
        catch (Throwable error) {
            return null;
        }
    }

    public static @Nullable String toFingerprint(final int hash1, final int hash2) {
        return toFingerprint(((long) hash1 << 32) | hash2);
    }

    public static @Nullable String toFingerprint(final String value) {
        try {
            final var digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes(StandardCharsets.UTF_8));
            final var fingerprintData = digest.digest();
            final var builder = new StringBuilder();
            for (final var b : fingerprintData) {
                builder.append(String.format("%02X", b));
            }
            return builder.toString();
        }
        catch (Throwable error) {
            return null;
        }
    }

    public static @Nullable String getFingerprint(final Path path) {
        try (final var stream = Files.newInputStream(path)) {
            final var digest = MessageDigest.getInstance("MD5");
            digest.update(stream.readAllBytes());
            final var fingerprintData = digest.digest();
            final var builder = new StringBuilder();
            for (final var b : fingerprintData) {
                builder.append(String.format("%02X", b));
            }
            return builder.toString();
        }
        catch (Throwable error) {
            return null;
        }
    }
}
