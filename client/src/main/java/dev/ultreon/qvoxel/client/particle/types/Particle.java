/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.client.particle.types;

import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.world.ClientWorld;
import dev.ultreon.qvoxel.resource.GameObject;
import dev.ultreon.qvoxel.world.World;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3f;

public abstract class Particle extends GameObject {
    public static final long FLAG_COLLISION = 1L;
    public static final long FLAG_GRAVITY = 2L;

    private final Mesh mesh;
    private final long baseTtl;
    private final Vector3f speed;
    public final Vector3f velocity = new Vector3f();
    private long flags = FLAG_COLLISION | FLAG_GRAVITY;
    private long ttl;

    public Particle(long baseTtl, Mesh mesh, Vector3f speed) {
        if (mesh == null) {
            throw new IllegalArgumentException("Mesh cannot be null");
        }

        this.baseTtl = baseTtl;
        this.mesh = mesh;
        this.speed = speed;
    }

    public Particle(Particle baseParticle) {
        mesh = baseParticle.getMesh();
        Vector3d aux = baseParticle.position;
        position.set(aux.x, aux.y, aux.z);
        Vector3f tmp = new Vector3f();
        Vector3f aux1 = baseParticle.getRotation(tmp);
        setRotation(aux1.x, aux1.y, aux1.z);
        setScale(baseParticle.scale.x, baseParticle.scale.y, baseParticle.scale.z);
        speed = new Vector3f(baseParticle.speed);
        ttl = baseParticle.geTtl();
        baseTtl = baseParticle.geTtl();
        flags = baseParticle.flags;
    }
    
    public Vector3f getSpeed() {
        return speed;
    }

    public void setSpeed(Vector3f speed) {
        this.speed.set(speed);
    }

    public long geTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    /**
     * Updates the particle's TTL
     * @param elapsedTime elapsed time in milliseconds
     * @return the particle's TTL
     */

    public long updateTtl(long elapsedTime) {
        ttl -= elapsedTime;
        return ttl;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public double getTime() {
        return (double) ttl / baseTtl;
    }

    @MustBeInvokedByOverriders
    public void draw(Mesh mesh, ShaderProgram particlesShaderProgram) {
        mesh.render(particlesShaderProgram);
    }

    public long getFlags() {
        return flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public boolean hasFlag(long flag) {
        return (flags & flag) != 0;
    }

    public void addFlag(long flag) {
        flags |= flag;
    }

    public void removeFlag(long flag) {
        flags &= ~flag;
    }

    public void tick(ClientWorld world) {
        if (hasFlag(FLAG_GRAVITY)) {
            velocity.add(0, -0.03f, 0);
            velocity.mul(0.98f);
        }

        if (hasFlag(FLAG_COLLISION) && move(world, velocity.x, velocity.y, velocity.z, false)) {
            velocity.set(0, 0, 0);
        } else {
            position.add(velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    protected boolean moveWithCollision(@NotNull World world, double dx, double dy, double dz, boolean validate) {
        return hasFlag(FLAG_COLLISION) && world.get((int) Math.floor(position.x + dx), (int) Math.floor(position.y + dy), (int) Math.floor(position.z + dz)).isSolid();
    }

    public void close() {

    }
}
