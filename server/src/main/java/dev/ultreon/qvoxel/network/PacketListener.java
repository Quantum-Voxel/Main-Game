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

package dev.ultreon.qvoxel.network;

public interface PacketListener {
    static PacketListener onFailure(Runnable func) {
        return new PacketListener() {
            @Override
            public void onFailure() {
                func.run();
            }
        };
    }

    static PacketListener onSuccess(Runnable func) {
        return new PacketListener() {
            @Override
            public void onSuccess() {
                func.run();
            }
        };
    }

    static PacketListener onEither(Runnable func) {
        return new PacketListener() {
            @Override
            public void onSuccess() {
                func.run();
            }

            @Override
            public void onFailure() {
                func.run();
            }
        };
    }

    default void onSuccess() {

    }

    default void onSent() {

    }

    default void onFailure() {

    }
}
