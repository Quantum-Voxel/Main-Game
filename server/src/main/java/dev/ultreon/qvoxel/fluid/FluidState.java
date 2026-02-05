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

package dev.ultreon.qvoxel.fluid;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.ubo.types.MapType;

import java.util.Objects;

/**
 * Represents the state of a fluid, including its type and level.
 * This class is an immutable representation of a fluid's quantity
 * and its associated type.
 */
public class FluidState {
    private final Fluid fluid;

    /**
     * Using milli-buckets (mB) as the unit. 1000mB = 1B.
     * The minimum level is 0mB, the maximum is 1000mB. One block is 1B.
     */
    private int level;

    public FluidState(Fluid fluid, int level) {
        this.fluid = fluid;
        this.level = level;
    }

    /**
     * Retrieves the current level of the fluid in milli-buckets (mB).
     * The level represents the quantity of the associated fluid, where 1000mB equals 1 bucket.
     * A level of 0 indicates an empty fluid state.
     *
     * @return the fluid level in milli-buckets (mB)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the level of the fluid in milli-buckets (mB).
     * The level represents the quantity of the fluid, where 1000mB equals 1 bucket.
     * A valid level must be within the expected range, such as 0mB to 1000mB.
     *
     * @param level the desired fluid level to set, measured in milli-buckets (mB)
     */
    public void setLevel(int level) {
        this.level = Math.clamp(level, 0, 1000);
    }

    public Fluid getFluid() {
        return fluid;
    }

    public boolean isEmpty() {
        return level <= 0 || fluid == Fluids.EMPTY;
    }

    public MapType save() {
        MapType map = new MapType();
        map.putString("fluid", String.valueOf(fluid.getId()));
        map.putInt("level", level);
        return map;
    }

    public static FluidState load(MapType map) {
        var fluid = Registries.FLUID.get(Identifier.parse(map.getString("fluid")));
        return new FluidState(fluid, map.getInt("level"));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FluidState that = (FluidState) o;
        return level == that.level && Objects.equals(fluid, that.fluid);
    }

    @Override
    public int hashCode() {
        int result = fluid.hashCode();
        result = 31 * result + level;
        return result;
    }

    @Override
    public String toString() {
        return "<fluid-state " + fluid + " / " + level + '>';
    }
}
