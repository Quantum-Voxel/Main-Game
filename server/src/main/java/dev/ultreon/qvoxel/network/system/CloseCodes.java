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

package dev.ultreon.qvoxel.network.system;

public enum CloseCodes {
    NORMAL_CLOSURE(1000),
    PROTOCOL_ERROR(1002),
    CLOSED_ABNORMALLY(1006),
    UNEXPECTED_CONDITION(1011),
    VIOLATED_POLICY(1012);

    private final int code;

    CloseCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
