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

package dev.ultreon.qvoxel.block.state.property;

class PropertyInfo {
    final int offset;  // bit offset
    final int bits;    // number of bits
    final int mask;    // ((1 << bits) - 1) << offset

    PropertyInfo(int offset, int bits) {
        this.offset = offset;
        this.bits = bits;
        mask = (1 << bits) - 1 << offset;
    }
}