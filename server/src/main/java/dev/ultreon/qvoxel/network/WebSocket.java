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

package dev.ultreon.qvoxel.network;

import org.jetbrains.annotations.Nullable;

public interface WebSocket {
    void addCloseListener(CloseListener listener);

    void removeCloseListener(CloseListener listener);

    void addOpenListener(OpenListener listener);

    void removeOpenListener(OpenListener listener);

    void addReceiveListener(ReceiveListener listener);

    void removeReceiveListener(ReceiveListener listener);

    void send(byte[] data, @Nullable PacketListener resultListener);

    default void onInternalError(Throwable throwable) {
        disconnect(500, "Internal error: " + throwable.toString());
    }

    void disconnect(int statusCode, String reason);

    void close();

    boolean isAlive();

    @FunctionalInterface
    interface CloseListener {
        void handle(int statusCode, String message);
    }

    @FunctionalInterface
    interface ReceiveListener {
        boolean handle(byte[] data);
    }

    @FunctionalInterface
    interface ConnectedListener {
        void handle(WebSocket socket);
    }

    @FunctionalInterface
    interface InitializeListener {
        void handle(WebSocket socket);
    }

    @FunctionalInterface
    interface OpenListener {
        void handle(WebSocket socket);
    }
}
