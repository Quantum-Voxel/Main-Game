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

package dev.ultreon.qvoxel.world;

import dev.ultreon.qvoxel.entity.Entity;
import dev.ultreon.ubo.types.MapType;

public abstract class LivingEntity extends Entity {
    protected float health;
    private float damageTilt;
    private boolean dead;

    protected LivingEntity(World world) {
        super(world);
        health = getMaxHealth();
    }

    @Override
    public MapType save() {
        MapType save = super.save();
        save.putFloat("health", health);
        save.putFloat("maxHealth", getMaxHealth());
        return save;
    }

    @Override
    public void load(MapType map) {
        health = map.getFloat("health");
        setDead(map.getBoolean("dead", false));
        super.load(map);
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = Math.clamp(health, 0, getMaxHealth());
    }

    public float getMaxHealth() {
        return 1;
    }

    public void damage(DamageSource source, float v) {
        health -= v;
        health = Math.max(health, 0);
        if (health <= 0) {
            health = 0;
            setDead(true);
        }

        damageTilt = 4f;
        damageTilt += v * 0.02f;
        damageTilt = Math.clamp(damageTilt, 0, 8);
    }

    public float getDamageTilt() {
        return damageTilt;
    }

    public void setDead(boolean dead) {
        if (dead) {
            health = 0;
        } else if (!this.dead) {
            health = getMaxHealth();
        }
        this.dead = dead;
        if (dead) {
            onDeath();
        } else {
            onResurrect();
        }
    }

    protected void onResurrect() {
        // Override to perform resurrection logic.
    }

    public void onDeath() {
        // Override to perform death logic.
    }

    public boolean isDead() {
        return dead;
    }

    @Override
    protected void hitGround(float fallDistance) {
        super.hitGround(fallDistance);

        World world = getWorld();
        if (world != null && !isInvincible() && !world.isClientSide()) {
            float damage = (fallDistance - 2f) / 2f;
            if (damage >= 1f)
                damage(new DamageSource(DamageType.FALL, this, null, position), (int) damage);
        }
    }

    public boolean isInvincible() {
        return false;
    }
}
