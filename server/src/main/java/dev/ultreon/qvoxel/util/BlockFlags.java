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

public class BlockFlags {
    public static final int NONE = 0;
    public static final int NOTIFY_CLIENTS = 1;
    public static final int RENDER = 2;
    public static final int BLOCK_UPDATE = 4;
    public static final int NEIGHBOR_UPDATE = 8;
    public static final int LIGHT_UPDATES = 16;
    public static final int RERENDER = 32;
    public static final int DROPS = 64;
}
