package dev.ultreon.qvoxel.world.gen;

public enum GenerationBarrier {
    NONE,
    FEATURE_INFO,
    CARVED,
    TERRAIN,
    FEATURES,
    STRUCTURES,
    LIGHTING,
    ORES,
    ALL;

    public static final GenerationBarrier SPAWN = ALL;
    public static final GenerationBarrier PRE_LIGHTING = STRUCTURES;

    public boolean includes(GenerationBarrier barrier) {
        return isAfter(barrier) || equals(barrier);
    }

    public boolean excludes(GenerationBarrier barrier) {
        return isBefore(barrier);
    }

    public boolean isAfter(GenerationBarrier barrier) {
        return ordinal() > barrier.ordinal();
    }

    public boolean isBefore(GenerationBarrier barrier) {
        return ordinal() < barrier.ordinal();
    }
}
