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

import dev.ultreon.qvoxel.util.Deletable;

/// Represents a generic OpenGL object. This interface provides the foundational
/// methods required for managing and interacting with OpenGL objects. Implementations
/// of this interface typically correspond to specific types of OpenGL resources,
/// such as textures, buffers, or shaders.
///
/// It is the responsibility of specific implementations to define the behavior of
/// these methods in accordance with the management of the associated OpenGL resource.
///
/// Methods include retrieving the unique identifier of the OpenGL object and deleting
/// the object when it is no longer necessary, ensuring proper resource cleanup.
///
/// Implementations should ensure thread safety if objects are accessed or modified
/// across different threads. Improper deletion or misuse of OpenGL objects may lead
/// to undefined behavior or runtime errors.
public interface GLObject extends Deletable {
    /// Retrieves the unique identifier associated with the OpenGL object.
    ///
    /// @return the integer identifier for the OpenGL object.
    int getObjectId();

    /// Deletes the associated OpenGL object. This method is intended to release any
    /// system resources or memory tied to the OpenGL object instance. The behavior
    /// and implementation of this method depend on the specific type of GLObject it
    /// is invoked on.
    ///
    /// After invoking this method, the associated OpenGL object might no longer
    /// be valid for use. Ensure that this method is only called when the object
    /// is no longer needed.
    void delete();
}
