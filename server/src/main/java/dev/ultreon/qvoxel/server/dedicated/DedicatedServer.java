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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.libs.crash.v0.ApplicationCrash;
import dev.ultreon.libs.crash.v0.CrashLog;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.network.system.NetworkInitializer;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.WorldStorage;
import dev.ultreon.qvoxel.spark.QuantumServerSparkPlugin;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioIoHandler;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DedicatedServer extends QuantumServer implements AutoCloseable {
    private static final EventLoopGroup EVENT_LOOP = new MultiThreadIoEventLoopGroup(Runtime.getRuntime().availableProcessors(), NioIoHandler.newFactory());
    private final DedicatedServerConfig config;
    private final String host;
    private final QuantumServerSparkPlugin sparkPlugin = new QuantumServerSparkPlugin();

    private DedicatedServer(String[] args) {
        Identifier.setDefaultNamespace(CommonConstants.NAMESPACE);

        DedicatedServerConfig config = DedicatedServerConfig.load();
        this.config = config;

        String hostname = config.hostname;
        int port = config.port;
        String path = config.path;
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        for (String s : argsList) {
            if (s.equals("--hostname")) {
                hostname = s;
            }
            if (s.equals("--port")) {
                try {
                    port = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    CommonConstants.LOGGER.error("Invalid port number: {}", s);
                    System.exit(1);
                }
            }
            if (s.equals("--path")) {
                path = s;
            }
        }
        host = hostname;

        super(new WorldStorage(Path.of(config.levelName)), FeatureSet.NONE);

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channelFactory(LocalServerChannel::new);
        serverBootstrap.group(EVENT_LOOP);
        serverBootstrap.handler(new NetworkInitializer(this));
        serverBootstrap.bind(config.hostname, config.port);

        try {
            CommonConstants.LOGGER.info("WebSocket server running at ws://{}:{}/{}", host, port, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public URI getServerUri() {
        return URI.create(host);
    }

    @Override
    protected long getSeed() {
        return config.seed;
    }

    @Override
    public void crash(CrashLog crashLog) {
        ApplicationCrash crash = crashLog.createCrash();
        CrashLog log = crash.getCrashLog();
        log.writeToFile(new File("crashes/crash-" + System.currentTimeMillis() + ".txt"));
        String string = log.toString();
        CommonConstants.LOGGER.error("An error occurred while running the server.\n{}", string);
        Runtime.getRuntime().halt(1);
    }

    @Override
    public boolean isDedicated() {
        return false;
    }

    static DedicatedServer create(String[] args) {
        if (QuantumServer.get() != null) throw new IllegalStateException("Server is already running!");

        return new DedicatedServer(args);
    }

    public void mainloop() {
        run();
    }

    @Override
    protected void save() {
        super.save();

        CommonConstants.LOGGER.info("Saving player data...");
        getPlayerManager().saveAll();
    }

    @Override
    public void close() {
        CommonConstants.LOGGER.info("Closing server...");
        shutdown(() -> {
            CommonConstants.LOGGER.info("Closed server...");
        });
    }

    public static DedicatedServer get() {
        return (DedicatedServer) QuantumServer.get();
    }

    @Override
    public QuantumServerSparkPlugin getSparkPlugin() {
        return sparkPlugin;
    }
}
