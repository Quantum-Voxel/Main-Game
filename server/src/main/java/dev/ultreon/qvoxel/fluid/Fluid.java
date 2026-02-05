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
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.World;

/**
 * Represents a type of fluid that can be created using a specified factory and settings.
 * This class defines the behavior and characteristics of a fluid through its configuration.
 */
public record Fluid(Settings settings) {
    public FluidState createFluid(World world, BlockVec blockVec) {
        return new FluidState(this, 1000);
    }

    public float density() {
        return settings.density;
    }

    public float viscosity() {
        return settings.viscosity;
    }

    public float temperature() {
        return settings.temperature;
    }

    public Identifier getId() {
        return Registries.FLUID.getId(this);
    }

    public FluidState getState(int level) {
        return new FluidState(this, level);
    }

    public interface FluidFactory {
        FluidState createFluid(World world, BlockVec blockVec);
    }

    /**
     * Represents the settings used to configure properties of a fluid.
     * Allows customization of density, viscosity, and temperature.
     * Methods in this class support method chaining.
     */
    public static class Settings {
        private float density = 1; // kg/m³
        private float viscosity = 1; // m³/s
        private float temperature = 298; // °K (Kelvin)

        /**
         * Sets the density of the fluid in the settings.
         *
         * @param density the density to set, measured in kilograms per cubic meter (kg/m³)
         * @return the current instance of {@code Settings} for method chaining
         */
        public Settings density(float density) {
            this.density = density;
            return this;
        }

        /**
         * Sets the viscosity of the fluid in the settings.
         *
         * @param viscosity the viscosity to set, measured in cubic meters per second (m³/s)
         * @return the current instance of {@code Settings} for method chaining
         */
        public Settings viscosity(float viscosity) {
            this.viscosity = viscosity;
            return this;
        }

        /**
         * Sets the temperature of this fluid's settings.
         *
         * @param temperature the temperature to set, measured in Kelvin
         * @return the current instance of {@code Settings} for method chaining
         */
        public Settings temperature(float temperature) {
            this.temperature = temperature;
            return this;
        }
    }
}
