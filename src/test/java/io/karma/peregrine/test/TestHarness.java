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

package io.karma.peregrine.test;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple headless OpenGL test harness that works with JUnit.
 * This can be used to integration-test implementations of graphics
 * API components inside and outside of Peregrine.
 *
 * @author Alexander Hinze
 * @since 16/09/2024
 */
public final class TestHarness implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger("Peregrine Test Harness");

    private final RenderFunction function;
    private final Thread thread = new Thread(this::threadMain);
    private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private long window; // Handle to our pseudo-window
    private int frameIndex;

    public TestHarness(final RenderFunction function) {
        this.function = function;
    }

    private void setupWindow() {
        GLFW.glfwInitHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_OSMESA_CONTEXT_API);
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }
        LOGGER.info("Initialized GLFW with OSMesa backend");

        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_API, GLFW.GLFW_OPENGL_FORWARD_COMPAT);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        window = GLFW.glfwCreateWindow(1920,
            1080,
            "Peregrine Test Harness",
            GLFW.glfwGetPrimaryMonitor(),
            MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create virtual framebuffer");
        }
        LOGGER.info("Created virtual framebuffer window");
        GLFW.glfwSwapInterval(1); // Always use "v-sync"
    }

    private void threadMain() {
        setupWindow();
        while (isRunning.get()) {
            GL11.glClearColor(0F, 0F, 0F, 1F);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            while (!tasks.isEmpty()) {
                tasks.remove().run();
            }
            if (!function.render(frameIndex++, tasks::add)) {
                close();
                break;
            }
            GLFW.glfwSwapBuffers(window);
        }
    }

    public void enqueue(final Runnable runnable) {
        if (Thread.currentThread() == thread) {
            runnable.run();
            return;
        }
        tasks.add(runnable);
    }

    public void run() {
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }
        thread.start();
    }

    @Override
    public void close() {
        if (!isRunning.compareAndSet(true, false)) {
            return;
        }
        try {
            thread.join();
            GLFW.glfwDestroyWindow(window);
        }
        catch (Throwable error) {
            LOGGER.error("Could not stop test harness render thread", error);
        }
    }
}
