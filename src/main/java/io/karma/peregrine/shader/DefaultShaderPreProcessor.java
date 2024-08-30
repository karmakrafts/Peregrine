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

import io.karma.peregrine.PeregrineMod;
import io.karma.peregrine.util.ToBooleanBiFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexander Hinze
 * @since 30/08/2024
 */
@OnlyIn(Dist.CLIENT)
public final class DefaultShaderPreProcessor implements ShaderPreProcessor {
    private static final Pattern SPECIAL_CONST_PATTERN = Pattern.compile(
        "\\b(special)\\s+(const)\\s+(\\w+)\\s+(\\w+)(\\s*?=\\s*?([\\w.\"'+\\-*/%]+))?\\s*?;");
    private static final Pattern INCLUDE_PATTERN = Pattern.compile(
        "(#include)\\s*?((\\s*?<((\\w+(:))?[\\w/._\\-]+)\\s*?>)|(\"\\s*?([\\w/._\\-]+)\\s*?\"))");
    private static final Pattern GL_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final char[] ESCAPABLE_CHARS = "\\\"nt".toCharArray();
    private static final int PRINT_BUFFER_SIZE = 4096;

    private static void processGreedy(final StringBuffer buffer,
                                      final Pattern pattern,
                                      final ToBooleanBiFunction<Matcher, StringBuffer> callback) {
        var matcher = pattern.matcher(buffer);
        while (matcher.find()) {
            if (!callback.apply(matcher, buffer)) {
                break;
            }
            matcher = pattern.matcher(buffer);
        }
    }

    private static String resolveRelativePath(final String path) {
        final var stack = new Stack<String>();
        final var parts = path.split("/");
        for (final var part : parts) {
            if (part.equals("..")) {
                stack.pop();
                continue;
            }
            stack.push(part);
        }
        return String.join("/", stack);
    }

    private static String getParentPath(final String path) {
        return path.substring(0, path.lastIndexOf('/'));
    }

    private static CharSequence generateStringData(final CharSequence sequence) {
        final var builder = new StringBuilder();
        builder.append("uint[]{");
        final var literalLength = sequence.length();
        for (var j = 0; j < literalLength; j++) {
            builder.append("0x").append(Integer.toHexString(sequence.charAt(j)));
            if (j < literalLength - 1) {
                builder.append(',');
            }
        }
        builder.append('}');
        return builder;
    }

    private static void stripCommentsAndWhitespace(final StringBuffer buffer) {
        final var source = buffer.toString();
        buffer.delete(0, buffer.length());
        var isBlockComment = false;
        var isLineComment = false;
        var skipNext = false;
        for (var i = 0; i < source.length(); i++) {
            final var c = source.charAt(i);
            // Pop/skip logic
            if (skipNext) {
                skipNext = false;
                continue;
            }
            final var hasNext = i < source.length() - 1;
            final var next = hasNext ? source.charAt(i + 1) : ' ';
            if (isBlockComment) {
                if (c == '*' && hasNext && next == '/') {
                    isBlockComment = false;
                    skipNext = true;
                }
                continue;
            }
            else if (isLineComment) {
                if (c == '\n') {
                    isLineComment = false;
                    buffer.append(c);
                }
                continue;
            }
            // Push logic
            if (c == '/' && hasNext && next == '*') {
                isBlockComment = true;
                continue;
            }
            if (c == '/' && hasNext && next == '/') {
                isLineComment = true;
                continue;
            }
            buffer.append(c);
        }
        final var lines = buffer.toString().split("\n");
        buffer.delete(0, buffer.length());
        for (final var line : lines) {
            if (line.isBlank()) { // Filter out any empty lines
                continue;
            }
            buffer.append(line).append("\n");
        }
    }

    private static String expandIncludesRecursively(final ResourceLocation location,
                                                    final String source,
                                                    final Function<ResourceLocation, String> loader,
                                                    final HashSet<ResourceLocation> includedLocations) {
        final var buffer = new StringBuffer(source);
        processGreedy(buffer, INCLUDE_PATTERN, (matcher, currentBuffer) -> {
            ResourceLocation targetLocation;
            final var relativePath = matcher.group(8);
            if (relativePath != null) { // We have a relative include path
                final var parentPath = getParentPath(location.getPath());
                final var joinedPath = String.format("%s/%s", parentPath, relativePath);
                targetLocation = new ResourceLocation(location.getNamespace(), resolveRelativePath(joinedPath));
            }
            else {
                final var path = matcher.group(4); // Otherwise grab the absolute one
                targetLocation = ResourceLocation.tryParse(path);
                if (targetLocation == null) {
                    throw new IllegalStateException(String.format("Malformed include location '%s'", path));
                }
            }
            if (includedLocations.contains(targetLocation)) {
                return true;
            }
            currentBuffer.replace(matcher.start(),
                matcher.end(),
                expandIncludesRecursively(targetLocation, loader.apply(targetLocation), loader, includedLocations));
            includedLocations.add(targetLocation);
            return true;
        });
        return buffer.toString();
    }

    private static void processIncludes(final ResourceLocation location,
                                        final StringBuffer buffer,
                                        final Function<ResourceLocation, String> loader) {
        final var source = buffer.toString();
        buffer.delete(0, buffer.length());
        buffer.append(expandIncludesRecursively(location, source, loader, new HashSet<>()));
    }

    private static void processSpecializationConstants(final ResourceLocation location,
                                                       final Map<String, Object> constants,
                                                       final StringBuffer buffer) {
        processGreedy(buffer, SPECIAL_CONST_PATTERN, (matcher, currentBuffer) -> {
            final var replacement = new StringBuilder();
            final var type = matcher.group(3);
            final var name = matcher.group(4);
            replacement.append(String.format("const %s %s", type, name));
            final var defaultValue = matcher.group(6);
            final var value = constants.get(name);
            if (defaultValue != null) { // We have a default value
                replacement.append(String.format(" = %s", Objects.requireNonNullElse(value, defaultValue)));
            }
            else {
                if (value == null) {
                    throw new IllegalStateException(String.format("Value for constant '%s' in object %s not defined",
                        name,
                        location));
                }
                replacement.append(String.format(" = %s", value));
            }
            replacement.append(';');
            currentBuffer.replace(matcher.start(), matcher.end(), replacement.toString());
            return true;
        });
    }

    private static void processDefines(final Map<String, Object> defines, final StringBuffer buffer) {
        final var defineBlock = new StringBuilder();
        for (final var define : defines.entrySet()) {
            defineBlock.append(String.format("#define %s %s\n", define.getKey(), define.getValue()));
        }
        buffer.insert(buffer.indexOf("\n", buffer.indexOf("#version")) + 1,
            defineBlock); // Always skip the first line for the version
    }

    private static Map<String, Object> insertBuiltinDefines(final ShaderType type, final Map<String, Object> defines) {
        final var allDefines = new LinkedHashMap<>(defines);
        allDefines.put("BUILTIN_DEBUG", PeregrineMod.isDevelopmentEnvironment() ? 1 : 0);
        allDefines.put("BUILTIN_PRINT_BUFFER_SIZE", PRINT_BUFFER_SIZE);
        allDefines.put("BUILTIN_SHADER_TYPE", type.ordinal());
        allDefines.put("BUILTIN_HAS_SODIUM", PeregrineMod.isSodiumInstalled() ? 1 : 0);
        allDefines.put("BUILTIN_HAS_IRIS", PeregrineMod.isIrisInstalled() ? 1 : 0);

        // TODO: fix these
        //allDefines.put("BUILTIN_BINDLESS_SUPPORT", StaticSampler.IS_SUPPORTED ? 1 : 0);
        //allDefines.put("BUILTIN_SSBO_SUPPORT", SSBO.IS_SUPPORTED ? 1 : 0);
        //allDefines.put("BUILTIN_LONG_SUPPORT", DefaultUniformType.LONG.isSupported() ? 1 : 0);
        //allDefines.put("BUILTIN_DOUBLE_SUPPORT", 0); // TODO: implement this

        final var glVersion = Objects.requireNonNull(GL11.glGetString(GL11.GL_VERSION));
        final var glVersionMatcher = GL_VERSION_PATTERN.matcher(glVersion);
        if (!glVersionMatcher.find()) {
            throw new IllegalStateException("Could not parse OpenGL version");
        }
        allDefines.put("BUILTIN_GL_MAJOR", glVersionMatcher.group(1));
        allDefines.put("BUILTIN_GL_MINOR", glVersionMatcher.group(2));
        allDefines.put("BUILTIN_GL_PATCH", glVersionMatcher.group(3));

        return allDefines;
    }

    private static Map<String, Object> insertBuiltinConstants(final Map<String, Object> constants) {
        final var allConstants = new LinkedHashMap<>(constants);
        allConstants.put("PRINT_BUFFER_SIZE", PRINT_BUFFER_SIZE);
        return allConstants;
    }

    @Override
    public String process(final String source,
                          final ShaderProgram program,
                          final ShaderObject object,
                          final Function<ResourceLocation, String> loader) {
        final var buffer = new StringBuffer(source);
        final var location = object.getLocation();

        processIncludes(location, buffer, loader);
        processSpecializationConstants(location, insertBuiltinConstants(program.getConstants()), buffer);
        processDefines(insertBuiltinDefines(object.getType(), program.getDefines()), buffer);
        stripCommentsAndWhitespace(buffer);

        return buffer.toString();
    }
}
