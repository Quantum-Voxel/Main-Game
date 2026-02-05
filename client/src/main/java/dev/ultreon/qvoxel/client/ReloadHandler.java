package dev.ultreon.qvoxel.client;

import dev.ultreon.qvoxel.client.shader.Reloadable;
import dev.ultreon.qvoxel.resource.ReloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class ReloadHandler {
    private final String name;
    private final Reloadable reloadable;
    final String[] deps;
    private CompletableFuture<?> waitBarrier;
    private CompletableFuture<?> process;

    public ReloadHandler(String name, Reloadable reloadable, String... deps) {
        this.name = name;
        this.reloadable = reloadable;
        this.deps = deps;
    }

    public CompletableFuture<Void> reload(ReloadContext context) {
        if (waitBarrier != null) {
            return waitBarrier.thenCompose(v -> actuallyReload(context));
        } else {
            return actuallyReload(context);
        }
    }

    private @NotNull CompletableFuture<Void> actuallyReload(ReloadContext context) {
        return reloadable.reload(context).thenRun(() -> process.complete(null));
    }

    public String name() {
        return name;
    }

    public Reloadable reloadable() {
        return reloadable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReloadHandler) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.reloadable, that.reloadable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, reloadable);
    }

    @Override
    public String toString() {
        return "ReloadHandler[" +
                "name=" + name + ", " +
                "reloadable=" + reloadable + ']';
    }

    public void start() {
        process = new CompletableFuture<>();
    }

    public void addWaitBarrier(Function<String, ReloadHandler> barrier) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (String dep : deps) {
            futures.add(barrier.apply(dep).process);
        }
        waitBarrier = CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new));
    }
}
