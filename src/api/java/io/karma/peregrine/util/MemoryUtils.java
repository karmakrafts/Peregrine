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

package io.karma.peregrine.util;

import io.karma.peregrine.Peregrine;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.LongPredicate;

import static org.lwjgl.system.Pointer.BITS32;
import static org.lwjgl.system.jni.JNINativeInterface.NewDirectByteBuffer;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
public final class MemoryUtils {
    private static final int MAGIC_CAPACITY = 0x0D15EA5E;
    private static final int MAGIC_POSITION = 0x00FACADE;
    private static final long ADDRESS;
    private static final long MARK;
    private static final long LIMIT;
    private static final long CAPACITY;
    private static final Class<? extends ByteBuffer> BYTE_BUFFER_TYPE;
    private static Unsafe unsafe;

    static {
        try {
            final var field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            field.setAccessible(false);
        }
        catch (Throwable error) {
            Peregrine.LOGGER.error("Could not retrieve sun.misc.Unsafe instance", error);
        }
        ADDRESS = getAddressOffset();
        MARK = getMarkOffset();
        LIMIT = getLimitOffset();
        CAPACITY = getCapacityOffset();
        BYTE_BUFFER_TYPE = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder()).getClass();
    }

    // @formatter:off
    private MemoryUtils() {}
    // @formatter:on

    private static long getFieldOffset(final Class<?> containerType,
                                       final Class<?> fieldType,
                                       final LongPredicate predicate) {
        Class<?> c = containerType;
        while (c != Object.class) {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                if (!field.getType().isAssignableFrom(fieldType) || Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }

                long offset = unsafe.objectFieldOffset(field);
                if (predicate.test(offset)) {
                    return offset;
                }
            }
            c = c.getSuperclass();
        }
        throw new UnsupportedOperationException("Failed to find field offset in class.");
    }

    private static long getAddressOffset() {
        long MAGIC_ADDRESS = 0xDEADBEEF8BADF00DL & (BITS32 ? 0xFFFF_FFFFL : 0xFFFF_FFFF_FFFF_FFFFL);
        ByteBuffer bb = Objects.requireNonNull(NewDirectByteBuffer(MAGIC_ADDRESS, 0));
        return getFieldOffset(bb.getClass(), long.class, offset -> unsafe.getLong(bb, offset) == MAGIC_ADDRESS);
    }

    private static long getFieldOffsetInt(final Object container, final int value) {
        return getFieldOffset(container.getClass(), int.class, offset -> unsafe.getInt(container, offset) == value);
    }

    private static long getMarkOffset() {
        ByteBuffer bb = Objects.requireNonNull(NewDirectByteBuffer(1L, 0));
        return getFieldOffsetInt(bb, -1);
    }

    private static long getLimitOffset() {
        ByteBuffer bb = Objects.requireNonNull(NewDirectByteBuffer(-1L, MAGIC_CAPACITY));
        bb.limit(MAGIC_POSITION);
        return getFieldOffsetInt(bb, MAGIC_POSITION);
    }

    private static long getCapacityOffset() {
        ByteBuffer bb = Objects.requireNonNull(NewDirectByteBuffer(-1L, MAGIC_CAPACITY));
        bb.limit(0);
        return getFieldOffsetInt(bb, MAGIC_CAPACITY);
    }

    public static ByteBuffer wrap(final long address, final int capacity) {
        ByteBuffer buffer;
        try {
            buffer = (ByteBuffer) unsafe.allocateInstance(BYTE_BUFFER_TYPE);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        unsafe.putLong(buffer, ADDRESS, address);
        unsafe.putInt(buffer, MARK, -1);
        unsafe.putInt(buffer, LIMIT, capacity);
        unsafe.putInt(buffer, CAPACITY, capacity);
        return buffer;
    }

    public static int align(final int value, final int alignment) {
        final var align = alignment - 1;
        return (value + align) & ~align;
    }
}
