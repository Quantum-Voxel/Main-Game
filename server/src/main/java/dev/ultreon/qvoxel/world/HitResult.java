package dev.ultreon.qvoxel.world;

import org.joml.Vector3d;

public interface HitResult {
    Vector3d hitPos();
    World world();
    double distance();
}
