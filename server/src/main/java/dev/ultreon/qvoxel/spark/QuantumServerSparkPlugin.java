package dev.ultreon.qvoxel.spark;

import dev.ultreon.qvoxel.CommonConstants;
import me.lucko.spark.common.platform.PlatformInfo;
import org.jetbrains.annotations.NotNull;

public class QuantumServerSparkPlugin extends QuantumSparkPlugin {
    @Override
    protected PlatformInfo.Type getType() {
        return PlatformInfo.Type.SERVER;
    }

    @Override
    public String getCommandName() {
        return "sparkc";
    }

    @Override
    protected void sendMessage(@NotNull String serialize) {
        CommonConstants.LOGGER.info(serialize);
    }
}
