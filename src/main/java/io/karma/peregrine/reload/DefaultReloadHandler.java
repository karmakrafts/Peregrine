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

package io.karma.peregrine.reload;

import io.karma.peregrine.Dispatcher;
import io.karma.peregrine.Peregrine;
import io.karma.peregrine.PeregrineMod;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

/**
 * @author Alexander Hinze
 * @since 29/08/2024
 */
public final class DefaultReloadHandler implements ReloadHandler, PreparableReloadListener {
    private final ConcurrentLinkedQueue<Reloadable> objects = new ConcurrentLinkedQueue<>();

    @Override
    public void register(final Reloadable reloadable) {
        if (objects.contains(reloadable)) {
            return;
        }
        objects.add(reloadable);
    }

    @Override
    public void unregister(final Reloadable reloadable) {
        objects.remove(reloadable);
    }

    @Override
    public List<Reloadable> getObjects() {
        final var sorted = new ArrayList<>(objects);
        sorted.sort(Reloadable.COMPARATOR);
        return sorted;
    }

    private List<Reloadable> getPrepSortedObjects() {
        final var sorted = new ArrayList<>(objects);
        sorted.sort(Reloadable.PREP_COMPARATOR);
        return sorted;
    }

    @Internal
    @Override
    public @NotNull CompletableFuture<Void> reload(final @NotNull PreparationBarrier barrier,
                                                   final @NotNull ResourceManager manager,
                                                   final @NotNull ProfilerFiller prepProfiler,
                                                   final @NotNull ProfilerFiller reloadProfiler,
                                                   final @NotNull Executor backgroundExecutor,
                                                   final @NotNull Executor gameExecutor) {
        // @formatter:off
        return CompletableFuture.supplyAsync(() -> prepareAll(manager, getPrepSortedObjects()), gameExecutor)
            .thenCompose(barrier::wait)
            .thenAcceptAsync(results -> reloadAll(manager, getObjects()), gameExecutor);
        // @formatter:on
    }

    private @Nullable Void prepareAll(final ResourceManager manager, final List<Reloadable> objects) {
        Peregrine.LOGGER.info("Preparing all resources");
        for (final var object : objects) {
            Peregrine.LOGGER.debug("Preparing resource {}", object);
            if (object.getPrepareDispatcher() == Dispatcher.BACKGROUND) {
                PeregrineMod.EXECUTOR_SERVICE.submit(() -> object.prepare(manager));
                continue;
            }
            object.prepare(manager);
        }
        return null;
    }

    private void reloadAll(final ResourceManager manager, final List<Reloadable> objects) {
        Peregrine.LOGGER.info("Reloading all resources");
        for (final var object : objects) {
            Peregrine.LOGGER.debug("Reloading resource {}", object);
            if (object.getReloadDispatcher() == Dispatcher.BACKGROUND) {
                PeregrineMod.EXECUTOR_SERVICE.submit(() -> object.reload(manager));
                continue;
            }
            object.reload(manager);
        }
    }
}
