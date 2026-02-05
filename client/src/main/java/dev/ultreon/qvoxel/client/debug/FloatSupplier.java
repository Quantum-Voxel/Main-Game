package dev.ultreon.qvoxel.client.debug;

import java.util.function.Supplier;

@FunctionalInterface
public interface FloatSupplier extends Supplier<Float> {
    float getFloat();

    @Override
    @Deprecated
    default Float get() {
        return getFloat();
    }
}
