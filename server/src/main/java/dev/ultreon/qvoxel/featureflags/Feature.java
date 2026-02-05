package dev.ultreon.qvoxel.featureflags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Feature(String id, @Nullable String namespace) {
    public Feature(String id) {
        this(id, null);
    }

    @Override
    public @NotNull String toString() {
        if (namespace != null) {
            return "Feature: " + id + " @ " + namespace;
        }
        return "Feature: " + id;
    }
}
