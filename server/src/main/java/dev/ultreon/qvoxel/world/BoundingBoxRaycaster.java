package dev.ultreon.qvoxel.world;

import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.entity.Entity;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;

public class BoundingBoxRaycaster {
    public static final class Ray {
        public final Vector3d origin;
        public final Vector3d dir;

        public Ray(Vector3d origin, Vector3d dir) {
            this.origin = origin;
            this.dir = dir.normalize();
        }
    }

    public enum Face {
        MIN_X, MAX_X, MIN_Y, MAX_Y, MIN_Z, MAX_Z;

        Direction toDirection() {
            return switch (this) {
                case MIN_X -> Direction.WEST;
                case MAX_X -> Direction.EAST;
                case MIN_Y -> Direction.DOWN;
                case MAX_Y -> Direction.UP;
                case MIN_Z -> Direction.NORTH;
                case MAX_Z -> Direction.SOUTH;
            };
        }
    }

    /**
     * Raycast using voxel traversal (Amanatides &amp; Woo). For each visited block, test any collision BoundingBoxs
     * returned by BlockState.getCollisionBoxes(). Returns the nearest hit within maxDistance or null.
     */
    public static HitResult rayCast(@Nullable Entity from, World world, Vector3d origin, Vector3d direction, double maxDistance) {
        Ray ray = new Ray(origin, direction);
        // Initial block coordinates
        int x = floorInt(origin.x);
        int y = floorInt(origin.y);
        int z = floorInt(origin.z);

        int stepX = sign(ray.dir.x);
        int stepY = sign(ray.dir.y);
        int stepZ = sign(ray.dir.z);

        double tMaxX = computeTMax(origin.x, ray.dir.x, x, stepX);
        double tMaxY = computeTMax(origin.y, ray.dir.y, y, stepY);
        double tMaxZ = computeTMax(origin.z, ray.dir.z, z, stepZ);

        double tDeltaX = (ray.dir.x == 0) ? Double.POSITIVE_INFINITY : Math.abs(1.0 / ray.dir.x);
        double tDeltaY = (ray.dir.y == 0) ? Double.POSITIVE_INFINITY : Math.abs(1.0 / ray.dir.y);
        double tDeltaZ = (ray.dir.z == 0) ? Double.POSITIVE_INFINITY : Math.abs(1.0 / ray.dir.z);

        double traveled = 0.0;
        double bestT = Double.POSITIVE_INFINITY;
        HitResult bestHit = null;

        for (Entity entity : world.getEntities()) {
            if (entity == from) continue;

            BoundingBox worldBox = entity.getBoundingBox();
            double t = worldBox.intersectRay(ray, 0.0, Math.min(maxDistance, bestT));
            if (t < bestT) {
                Vector3d hitPos = ray.origin.add(ray.dir.mul(t));
                bestT = t;
                bestHit = new EntityHitResult(hitPos, world, maxDistance, entity);
            }
        }

        // If origin starts inside a block, we want to test that block first
        for (int steps = 0; traveled <= maxDistance && steps < 2000; steps++) {
            BlockState state = world.get(x, y, z);
            if (!state.isAir()) {
                List<BoundingBox> boxes = state.getBoundingBoxes(world, new BlockVec(x, y, z));
                if (boxes != null) {
                    for (BoundingBox b : boxes) {
                        // Convert block-local BoundingBox [0..1] to world coordinates
                        BoundingBox worldBox = new BoundingBox(
                                x + b.min().x, y + b.min().y, z + b.min().z,
                                x + b.max().x, y + b.max().y, z + b.max().z
                        );
                        double t = worldBox.intersectRay(ray, 0.0, Math.min(maxDistance, bestT));
                        if (t < bestT) {
                            Vector3d hitPos = ray.origin.add(ray.dir.mul(t));
                            Face face = pickFace(worldBox, hitPos);
                            bestT = t;
                            BlockVec blockVec = new BlockVec(x, y, z);
                            Direction dir = face.toDirection();
                            bestHit = new BlockHitResult(blockVec, blockVec.offset(dir), hitPos, world, dir, maxDistance, state);
                        }
                    }
                }
            }

            // If we already hit something closer than the next voxel boundary, we can stop early.
            double nextBoundary = Math.min(Math.min(tMaxX, tMaxY), tMaxZ);
            if (bestHit != null && bestT <= nextBoundary) break;

            // Step to next voxel
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    traveled = tMaxX;
                    tMaxX += tDeltaX;
                } else {
                    z += stepZ;
                    traveled = tMaxZ;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY;
                    traveled = tMaxY;
                    tMaxY += tDeltaY;
                } else {
                    z += stepZ;
                    traveled = tMaxZ;
                    tMaxZ += tDeltaZ;
                }
            }

            if (traveled > maxDistance) break;
        }

        return bestHit;
    }

    // Helpers
    private static int floorInt(double v) {
        return (int) Math.floor(v);
    }

    private static int sign(double v) {
        return v > 0 ? 1 : negSign(v);
    }

    private static int negSign(double v) {
        return v < 0 ? -1 : 0;
    }

    private static double computeTMax(double originCoord, double dir, int blockCoord, int step) {
        if (dir == 0) return Double.POSITIVE_INFINITY;
        if (step > 0) {
            // next boundary is blockCoord + 1
            return ((blockCoord + 1) - originCoord) / dir;
        } else {
            // next boundary is blockCoord (we are stepping negative)
            return (blockCoord - originCoord) / dir;
        }
    }

    // Determine which face of the BoundingBox was hit by comparing the hit position to box faces with epsilon
    private static Face pickFace(BoundingBox box, Vector3d pos) {
        double eps = 1e-6;
        if (Math.abs(pos.x - box.min().x) < eps) return Face.MIN_X;
        if (Math.abs(pos.x - box.max().x) < eps) return Face.MAX_X;
        if (Math.abs(pos.y - box.min().y) < eps) return Face.MIN_Y;
        if (Math.abs(pos.y - box.max().y) < eps) return Face.MAX_Y;
        if (Math.abs(pos.z - box.min().z) < eps) return Face.MIN_Z;
        if (Math.abs(pos.z - box.max().z) < eps) return Face.MAX_Z;
        // fallback: pick nearest face by distance
        double dx = Math.min(Math.abs(pos.x - box.min().x), Math.abs(pos.x - box.max().x));
        double dy = Math.min(Math.abs(pos.y - box.min().y), Math.abs(pos.y - box.max().y));
        double dz = Math.min(Math.abs(pos.z - box.min().z), Math.abs(pos.z - box.max().z));
        if (dx <= dy && dx <= dz)
            return (Math.abs(pos.x - box.min().x) < Math.abs(pos.x - box.max().x)) ? Face.MIN_X : Face.MAX_X;
        if (dy <= dx && dy <= dz)
            return (Math.abs(pos.y - box.min().y) < Math.abs(pos.y - box.max().y)) ? Face.MIN_Y : Face.MAX_Y;
        return (Math.abs(pos.z - box.min().z) < Math.abs(pos.z - box.max().z)) ? Face.MIN_Z : Face.MAX_Z;
    }

}
