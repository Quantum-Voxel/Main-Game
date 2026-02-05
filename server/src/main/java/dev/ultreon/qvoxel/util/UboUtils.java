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

package dev.ultreon.qvoxel.util;

import dev.ultreon.ubo.types.MapType;
import org.joml.Vector3d;

public class UboUtils {
    private UboUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void putVector3d(MapType map, String name, Vector3d position1) {
        MapType position = new MapType();
        position.putDouble("x", position1.x);
        position.putDouble("y", position1.y);
        position.putDouble("z", position1.z);
        map.put(name, position);
    }

    public static Vector3d getVector3d(MapType map, String name, Vector3d out) {
        MapType position = map.get(name).cast(MapType.class, new MapType());
        double x = position.getDouble("x");
        double y = position.getDouble("y");
        double z = position.getDouble("z");
        return out.set(x, y, z);
    }
}
