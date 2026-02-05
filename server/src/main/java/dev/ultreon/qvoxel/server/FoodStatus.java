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

package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.network.NetworkSerializable;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.player.PlayerEntity;
import dev.ultreon.qvoxel.world.DamageSource;
import dev.ultreon.qvoxel.world.DamageType;
import dev.ultreon.qvoxel.world.Difficulty;
import dev.ultreon.ubo.types.MapType;
import org.joml.Vector3d;

public class FoodStatus implements NetworkSerializable {
    private int foodLevel;
    private float saturationLevel;
    private float exhaustionLevel;
    private int tickTimer;

    public FoodStatus(int foodLevel, float saturationLevel, float exhaustionLevel) {
        this.foodLevel = foodLevel;
        this.saturationLevel = saturationLevel;
        this.exhaustionLevel = exhaustionLevel;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public float getSaturationLevel() {
        return saturationLevel;
    }

    public float getExhaustionLevel() {
        return exhaustionLevel;
    }

    public void exhaust(float amount) {
        exhaustionLevel += amount;

        while (exhaustionLevel >= 4.0f) {
            exhaustionLevel -= 4.0f;

            if (saturationLevel > 0.0f) {
                saturationLevel = Math.max(saturationLevel - 1.0f, 0.0f);
            } else if (foodLevel > 0) {
                foodLevel = Math.max(foodLevel - 1, 0);
            }
        }
    }

    @Override
    public void write(PacketIO packetIO) {
        packetIO.writeInt(foodLevel);
        packetIO.writeFloat(saturationLevel);
        packetIO.writeFloat(exhaustionLevel);
    }

    public static FoodStatus read(PacketIO packetIO) {
        return new FoodStatus(packetIO.readInt(), packetIO.readFloat(), packetIO.readFloat());
    }

    public void tick(PlayerEntity player) {
        tickTimer++;

        // === Natural Regeneration ===
        if (foodLevel >= 18 && player.getHealth() < player.getMaxHealth()) {
            if (tickTimer >= 80) { // 4 seconds
                player.heal(1.0f);
                // Regen costs exhaustion
                exhaust(6.0f);
                tickTimer = 0;
            }
        }
        // === Starvation Damage ===
        else if (foodLevel <= 0) {
            if (tickTimer >= 80) { // 4 seconds
                // In MC: easy = never die, normal = down to 10 hp, hard = down to 1 hp
                if (player.getWorld().getDifficulty() == Difficulty.HARD || player.getWorld().getDifficulty() == Difficulty.IMPOSSIBLE ||
                        player.getWorld().getDifficulty() == Difficulty.NORMAL && player.getHealth() > 10.0f ||
                        player.getWorld().getDifficulty() == Difficulty.EASY && player.getHealth() > 1.0f) {
                    Vector3d forward = player.getForward();
                    player.damage(new DamageSource(DamageType.STARVE, null, null, new Vector3d(player.getPosition()).sub(0, player.getEyeHeight() / 3, 0).add(forward.mul(0.1, forward))), 1.0f);
                }
                tickTimer = 0;
            }
        } else {
            // reset timer if no regen or starvation applies
            tickTimer = 0;
        }
    }

    public void eat(Food food) {
        foodLevel += food.foodLevel();
        saturationLevel += food.saturationLevel();
        exhaustionLevel = 0;
    }

    public MapType save() {
        MapType entries = new MapType();
        entries.putInt("foodLevel", foodLevel);
        entries.putFloat("saturationLevel", saturationLevel);
        entries.putFloat("exhaustionLevel", exhaustionLevel);
        return entries;
    }

    public void load(MapType map) {
        foodLevel = map.getInt("foodLevel");
        saturationLevel = map.getFloat("saturationLevel");
        exhaustionLevel = map.getFloat("exhaustionLevel");
    }
}
