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

package dev.ultreon.qvoxel.client.model;

import java.util.Objects;

/**
 * The FaceProperties class represents the properties of a face, such as random rotation.
 * This class includes methods for equality comparison and hashing.
 * It also includes a Builder class to aid in creating instances of FaceProperties with the desired properties.
 */
public class FaceProperties {
    public boolean randomRotation = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaceProperties that = (FaceProperties) o;
        return randomRotation == that.randomRotation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(randomRotation);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * The Builder class is a helper class to aid in the creation of instances
     * of the FaceProperties class with specified properties.
     * <p>
     * It provides a fluent API for setting different properties and eventually
     * building the FaceProperties instance.
     */
    public static class Builder {
        private final FaceProperties faceProperties = new FaceProperties();

        public Builder randomRotation() {
            faceProperties.randomRotation = true;
            return this;
        }

        public FaceProperties build() {
            return faceProperties;
        }
    }
}
