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

package dev.ultreon.qvoxel;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.libs.commons.v0.Profiler;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.util.ExecutorClosedException;
import org.apache.commons.collections4.queue.SynchronizedQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * PollingExecutorService is an implementation of the ExecutorService that uses polling for task execution.
 * Tasks are kept in a synchronized queue and processed by a dedicated thread.
 */
@SuppressWarnings("NewApi")
public class PollingExecutorService extends GameNode implements Executor {
    protected final Queue<Runnable> tasks = SynchronizedQueue.synchronizedQueue(new ArrayDeque<>(2000));
    private final List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();
    protected Thread thread;
    private boolean isShutdown = false;
    @Nullable
    private Runnable active;
    public final Profiler profiler;

    /**
     * Internal constructor for creating an instance of PollingExecutorService using the current thread
     * and a Profiler instance for monitoring the execution.
     *
     * @param profiler The Profiler instance for monitoring the execution.
     */
    @ApiStatus.Internal
    public PollingExecutorService(Profiler profiler) {
        this(Thread.currentThread(), profiler);
    }

    /**
     * Internal constructor for creating an instance of PollingExecutorService using a specified thread
     * and a Profiler instance for monitoring the execution.
     *
     * @param thread   The thread to be associated with the PollingExecutorService instance.
     * @param profiler The Profiler instance for monitoring the execution.
     */
    @ApiStatus.Internal
    public PollingExecutorService(@NotNull Thread thread, Profiler profiler) {
        this.thread = thread;
        this.profiler = profiler;
    }

    public void shutdown(Runnable finalizer) {
        isShutdown = true;
        for (CompletableFuture<?> future : futures) {
            future.completeExceptionally(new ExecutorClosedException("Executor has been shut down"));
        }

        tasks.clear();
        futures.clear();

        finalizer.run();
    }

    public @NotNull List<Runnable> shutdownNow() {
        isShutdown = true;
        List<Runnable> remainingTasks = List.copyOf(tasks);
        tasks.clear();

        for (CompletableFuture<?> future : futures) {
            future.cancel(true);
        }
        futures.clear();
        return remainingTasks;
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public boolean isTerminated() {
        return isShutdown && tasks.isEmpty();
    }

    @SuppressWarnings("BusyWait")
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        var endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        while (!isTerminated() && System.currentTimeMillis() < endTime) Thread.sleep(100);
        return isTerminated();
    }

    public <T> @NotNull CompletableFuture<T> submit(@NotNull Callable<T> task) {
        var future = new CompletableFuture<T>();
        Throwable exception = new Throwable();
        if (isSameThread()) {
            try {
                future.complete(task.call());
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    Identifier id = ((Task<?>) task).id();
                    CommonConstants.LOGGER.warn("Submitted task failed \"{}\":", id, throwable);
                }
                future.completeExceptionally(throwable);
                throwable.addSuppressed(exception);
                CommonConstants.LOGGER.warn("Submitted task failed:", throwable);
            }
            return future;
        }
        execute(() -> profiler.section(task.getClass().getName(), () -> {
            try {
                var result = task.call();
                future.complete(result);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
                throwable.addSuppressed(exception);
                CommonConstants.LOGGER.warn("Submitted task failed:", throwable);
            }
        }));
        return future;
    }

    public <T> @NotNull CompletableFuture<T> submit(@NotNull Runnable task, T result) {
        CompletableFuture<T> future = new CompletableFuture<>();
        if (isSameThread()) {
            try {
                task.run();
                future.complete(result);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    Identifier id = ((Task<?>) task).id();
                    CommonConstants.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.completeExceptionally(throwable);
            }
            return future;
        }
        execute(() -> profiler.section(task.getClass().getName(), () -> {
            try {
                task.run();
                future.complete(result);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        }));
        return future;
    }

    public @NotNull CompletableFuture<Void> submit(@NotNull Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (isSameThread()) {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    Identifier id = ((Task<?>) task).id();
                    CommonConstants.LOGGER.warn("Submitted task failed \"" + id + "\":", throwable);
                }
                future.completeExceptionally(throwable);
            }
            return future;
        }
        execute(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable throwable) {
                if (task instanceof Task<?>) {
                    Identifier id = ((Task<?>) task).id();
                    CommonConstants.LOGGER.warn("Submitted task failed \"{}\":", id, throwable);
                }
                CommonConstants.LOGGER.error("Failed to run task:", throwable);
                future.completeExceptionally(throwable);
            }
            futures.remove(future);
        });

        futures.add(future);
        return future;
    }

    public <T> @NotNull List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submit)
                .toList();
        return futures.stream()
                .map(CompletableFuture::join)
                .map(CompletableFuture::completedFuture)
                .collect(Collectors.toList());
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (isShutdown)
            throw new ExecutorClosedException("Executor is already shut down");

        if (isSameThread()) {
            command.run();
            return;
        }

        tasks.add(command);
    }

    private boolean isSameThread() {
        return Thread.currentThread().threadId() == thread.threadId();
    }

    /**
     * Polls the next task from the queue and runs it if available.
     * If a task is polled, it will be executed, and any exceptions thrown during its execution
     * will be logged using the LOGGER.
     * <p>
     * This method is intended for internal use only and is primarily utilized within the
     * PollingExecutorService to process and execute tasks.
     */
    @ApiStatus.Internal
    public void poll() {
        profiler.section("pollTask", () -> {
            if ((active = tasks.poll()) != null) {
                try {
                    active.run();
                } catch (Throwable t) {
                    CommonConstants.LOGGER.error("Failed to run task:", t);
                }
            }
        });
    }

    /**
     * Polls and executes all tasks in the task queue.
     * <p>
     * This method continues to poll tasks from the queue and execute them
     * until there are no more tasks left in the queue. For each task polled
     * from the queue, it creates a profiling section named "pollTask" to monitor its execution.
     * <p>
     * If an exception occurs during the execution of a task, it is caught and logged
     * using the LOGGER instance.
     */
    public void pollAll() {
        while ((active = tasks.poll()) != null) {
            profiler.startSection("pollTask");
            try {
                var task = active;

                try {
                    task.run();
                } catch (Throwable t) {
                    CommonConstants.LOGGER.error("Failed to run task:", t);
                }
            } finally {
                profiler.endSection("pollTask");
            }
        }
    }

    /**
     * Returns the current size of the task queue.
     * <p>
     * This method returns the number of tasks currently in the task queue.
     * It can be used to determine the number of tasks that are waiting to be executed.
     *
     * @return the current size of the task queue.
     */
    public int getQueueSize() {
        return tasks.size();
    }
}
