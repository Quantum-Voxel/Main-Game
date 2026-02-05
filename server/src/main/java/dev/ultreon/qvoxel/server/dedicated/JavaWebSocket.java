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

package dev.ultreon.qvoxel.server.dedicated;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.PacketListener;
import dev.ultreon.qvoxel.network.WebSocket;
import dev.ultreon.qvoxel.network.system.CloseCodes;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class JavaWebSocket implements WebSocket, java.net.http.WebSocket.Listener {
    private final HttpClient client;
    private boolean connecting = true;
    private boolean connected = false;
    private java.net.http.WebSocket socket;
    private final Set<WebSocket.CloseListener> closeListeners = new HashSet<>();
    private final Set<WebSocket.ReceiveListener> receiveListeners = new HashSet<>();
    private final Set<WebSocket.OpenListener> openListeners = new HashSet<>();
    private final ConnectedListener listener;
    private final ByteArrayOutputStream data = new ByteArrayOutputStream();

    public JavaWebSocket(String location, Consumer<Throwable> onError, InitializeListener initializeListener, ConnectedListener listener) {
        client = HttpClient.newHttpClient();
        initializeListener.handle(this);
        this.listener = listener;

        client.newWebSocketBuilder().header("Ultreon-QuantumVoxel-Client", "Yes").header("User-Agent", "QuantumVoxel/" + FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow().getMetadata().getVersion().getFriendlyString()).buildAsync(URI.create(location), this).exceptionally(throwable -> {
            onError.accept(throwable);
            return null;
        });
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        closeListeners.add(listener);
    }

    @Override
    public void removeCloseListener(CloseListener listener) {
        closeListeners.remove(listener);
    }

    @Override
    public void addOpenListener(OpenListener listener) {
        openListeners.add(listener);
    }

    @Override
    public void removeOpenListener(OpenListener listener) {
        openListeners.remove(listener);
    }

    @Override
    public void addReceiveListener(ReceiveListener listener) {
        receiveListeners.add(listener);
    }

    @Override
    public void removeReceiveListener(ReceiveListener listener) {
        receiveListeners.remove(listener);
    }

    @Override
    public void send(byte[] data, @Nullable PacketListener resultListener) {
        socket.sendBinary(ByteBuffer.wrap(data), true).handle((socket, throwable) -> {
            if (resultListener != null) {
                if (throwable == null)
                    resultListener.onSuccess();
                else resultListener.onFailure();
            }

            return socket;
        });
    }

    @Override
    public void disconnect(int statusCode, String reason) {
        socket.sendClose(statusCode, reason).handle((socket, throwable) -> {
            if (throwable instanceof TimeoutException) socket.abort();
            return null;
        });
    }

    @Override
    public void close() {
        if (socket == null) return;
        socket.abort();
    }

    @Override
    public boolean isAlive() {
        return !socket.isInputClosed() && !socket.isOutputClosed();
    }

    @Override
    public void onOpen(java.net.http.WebSocket webSocket) {
        socket = webSocket;
        connected = true;
        connecting = false;
        listener.handle(this);
        java.net.http.WebSocket.Listener.super.onOpen(webSocket);
        for (OpenListener openListener : openListeners) {
            openListener.handle(this);
        }
    }

    @Override
    public CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
        return java.net.http.WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onBinary(java.net.http.WebSocket webSocket, ByteBuffer data, boolean last) {
        try {
            if (!last) {
                CommonConstants.LOGGER.warn("Received partial packet!");
                return CompletableFuture.completedFuture(null);
            }
            for (ReceiveListener receiveListener : receiveListeners) {
                byte[] bytes = new byte[data.remaining()];
                this.data.write(bytes);
                if (receiveListener.handle(this.data.toByteArray())) {
                    this.data.reset();
                    webSocket.request(1);
                    return CompletableFuture.completedFuture(null);
                }
                this.data.reset();
            }
            CommonConstants.LOGGER.error("Didn't handle packet! (This shouldn't happen)");
            disconnect(CloseCodes.UNEXPECTED_CONDITION.getCode(), "We didn't handle the packet!");
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to handle data:", e);
            disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "We didn't handle the packet!");
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
        for (CloseListener closeListener : closeListeners) {
            closeListener.handle(statusCode, reason);
        }
        return CompletableFuture.runAsync(webSocket::abort);
    }

    @Override
    public void onError(java.net.http.WebSocket webSocket, Throwable error) {
        disconnect(500, "Internal connection error!\n" + error.getLocalizedMessage());
        java.net.http.WebSocket.Listener.super.onError(webSocket, error);
    }
}
