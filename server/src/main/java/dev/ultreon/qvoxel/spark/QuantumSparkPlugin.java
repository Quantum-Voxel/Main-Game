package dev.ultreon.qvoxel.spark;

import dev.ultreon.qvoxel.server.QuantumServer;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.SparkPlugin;
import me.lucko.spark.common.command.sender.CommandSender;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.util.SparkThreadFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.stream.Stream;

public abstract class QuantumSparkPlugin implements SparkPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("Spark");
    private final CommandSender sender = new CommandSender() {
        @Override
        public String getName() {
            return "dummy";
        }

        @Override
        public UUID getUniqueId() {
            return new UUID(0, 0);
        }

        @Override
        public void sendMessage(Component component) {
            QuantumSparkPlugin.this.sendMessage(LegacyComponentSerializer.builder().character('\u0000').build().serialize(component));
        }

        @Override
        public boolean hasPermission(String s) {
            return true;
        }
    };
    private SparkPlatform platform;
    protected final ScheduledExecutorService scheduler;

    protected QuantumSparkPlugin() {
        scheduler = Executors.newScheduledThreadPool(4, new SparkThreadFactory());
    }

    public void enable() {
        platform = new SparkPlatform(this);
        platform.enable();
    }

    public void disable() {
        platform.disable();
    }

    @Override
    public String getVersion() {
        return FabricLoader.getInstance().getRawGameVersion();
    }

    @Override
    public Path getPluginDirectory() {
        return FabricLoader.getInstance().getGameDir().resolve("spark");
    }

    @Override
    public String getCommandName() {
        return "spark";
    }

    @Override
    public Stream<? extends CommandSender> getCommandSenders() {
        return Stream.of(sender);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        scheduler.execute(runnable);
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new PlatformInfo() {
            @Override
            public Type getType() {
                return QuantumSparkPlugin.this.getType();
            }

            @Override
            public String getName() {
                return "Quantum Voxel";
            }

            @Override
            public String getBrand() {
                return "Quantum Voxel";
            }

            @Override
            public String getVersion() {
                return FabricLoader.getInstance().getRawGameVersion();
            }

            @Override
            public String getMinecraftVersion() {
                return "none";
            }
        };
    }

    protected abstract PlatformInfo.Type getType();

    @Override
    public void log(Level level, String s) {
        if (level.equals(Level.INFO)) {
            LOGGER.info(s);
        } else if (level.equals(Level.WARNING)) {
            LOGGER.warn(s);
        } else if (level.equals(Level.SEVERE)) {
            LOGGER.error(s);
        } else if (level.equals(Level.CONFIG)) {
            LOGGER.debug(s);
        } else {
            LOGGER.trace(s);
        }
    }

    @Override
    public void log(Level level, String s, Throwable throwable) {
        if (level.equals(Level.INFO)) {
            LOGGER.info(s, throwable);
        } else if (level.equals(Level.WARNING)) {
            LOGGER.warn(s, throwable);
        } else if (level.equals(Level.SEVERE)) {
            LOGGER.error(s, throwable);
        } else if (level.equals(Level.CONFIG)) {
            LOGGER.debug(s, throwable);
        } else {
            LOGGER.trace(s, throwable);
        }
    }

    public void executeCommand(String[] args) {
        platform.executeCommand(sender, args);
    }

    protected abstract void sendMessage(@NotNull String serialize);
}
