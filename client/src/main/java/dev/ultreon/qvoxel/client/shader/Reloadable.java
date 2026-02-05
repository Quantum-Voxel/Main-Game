package dev.ultreon.qvoxel.client.shader;

import dev.ultreon.qvoxel.resource.ReloadContext;

import java.util.concurrent.CompletableFuture;

public interface Reloadable {
    CompletableFuture<?> reload(ReloadContext context);
}
