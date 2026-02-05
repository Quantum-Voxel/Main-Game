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

package dev.ultreon.qvoxel.client.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ultreon.qvoxel.world.Axis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Locale;

/**
 * Represents a rotational transformation applied to an element, defined by its origin, axis,
 * angle of rotation, and an optional rescaling flag.
 * <p>
 * This class is used to manage rotation information for elements in a 3D space, where the
 * rotation is specified with respect to a defined origin vector and a chosen axis (X, Y, or Z).
 * The amount of rotation is determined by an angle measured in degrees, and optionally, the
 * transformation can apply rescaling to maintain proportionality.
 * <p>
 * Instances of this class are immutable and provide methods to retrieve the properties of the rotation.
 */
public record ElementRotation(Vector3f originVec, Axis axis, float angle, boolean rescale) {
    public static final ElementRotation ZERO = new ElementRotation(new Vector3f(), Axis.X, 0f, false);

    /**
     * @param originVec the origin vector representing the point around which the element is rotated
     * @param axis      the axis of rotation (X, Y, or Z)
     * @param angle     the angle of rotation in degrees
     * @param rescale   whether to apply rescaling after the rotation
     */
    public ElementRotation {
    }

    public static ElementRotation deserialize(@Nullable JsonObject rotation) {
        if (rotation == null) {
            return new ElementRotation(new Vector3f(0, 0, 0), Axis.Y, 0, false);
        }

        JsonArray originJson = rotation.getAsJsonArray("origin");
        float[] origin = new float[3];
        origin[0] = originJson.get(0).getAsFloat();
        origin[1] = originJson.get(1).getAsFloat();
        origin[2] = originJson.get(2).getAsFloat();
        String axis = rotation.get("axis").getAsString();
        float angle = rotation.get("angle").getAsFloat();
        JsonElement rescale1 = rotation.get("rescale");
        boolean rescale = rescale1 == null || rescale1.getAsBoolean();

        Vector3f originVec = new Vector3f(origin[0], origin[1], origin[2]);
        return new ElementRotation(originVec, Axis.valueOf(axis.toUpperCase(Locale.ROOT)), angle, rescale);
    }

    @Override
    public @NotNull String toString() {
        return "ElementRotation[" +
                "originVec=" + originVec + ", " +
                "axis=" + axis + ", " +
                "angle=" + angle + ", " +
                "rescale=" + rescale + ']';
    }


}
