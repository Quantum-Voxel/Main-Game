/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.resource;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.PollingExecutorService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ReloadContext implements Executor {
    private final PollingExecutorService executor;
    private final List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();
    private final ResourceManager resourceManager;
    private Consumer<String> messageHandler = s -> {};

    public ReloadContext(PollingExecutorService executor, ResourceManager resourceManager) {
        this.executor = executor;
        this.resourceManager = resourceManager;
    }

    public static ReloadContext create(PollingExecutorService executor, ResourceManager resourceManager) {
        return new ReloadContext(executor, resourceManager);
    }

    /**
     * Submits a task safely to the client/server thread.
     */
    public CompletableFuture<Void> submitSafe(Runnable submission) {
        CompletableFuture<Void> submitted = executor.submit(submission);
        futures.add(submitted);
        return submitted;
    }

    /**
     * Submits a task safely to the client/server thread.
     */
    public @NotNull <T> CompletableFuture<T> submitSafe(Callable<T> submission) {
        @NotNull CompletableFuture<T> submitted = executor.submit(submission);
        futures.add(submitted);
        return submitted;
    }

    public boolean isDone() {
        return futures.stream().allMatch(Future::isDone);
    }

    public void finish() {
        if (!isDone()) {
            throw new IllegalStateException("Cannot close when not done");
        }

        futures.clear();
    }

    public void await() {
        for (CompletableFuture<?> future : futures) {
            try {
                future.join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        submitSafe(command);
    }

    public void setMessageHandler(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void log(String message) {
        messageHandler.accept(message);
    }
}
