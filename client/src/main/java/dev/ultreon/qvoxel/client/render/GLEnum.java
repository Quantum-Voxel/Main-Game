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

package dev.ultreon.qvoxel.client.render;

/**
 * GLEnum is an interface that represents a contract for enumerated types
 * associated with OpenGL constant values.
 * <p>
 * Implementing enums represent specific OpenGL enumerations, where each
 * constant corresponds to a unique integer value defined by the OpenGL API.
 * <p>
 * Classes implementing this interface must define the method {@link #getGLValue},
 * which retrieves the OpenGL integer value associated with an enumeration constant.
 * <p>
 * This interface is intended to standardize the mapping of OpenGL constant
 * values in Java, allowing for better type-safety and readability in OpenGL
 * codebases.
 */
public interface GLEnum {
    /**
     * Retrieves the OpenGL integer value associated with the enumeration constant.
     *
     * @return the integer value corresponding to the OpenGL constant.
     */
    int getGLValue();
}
