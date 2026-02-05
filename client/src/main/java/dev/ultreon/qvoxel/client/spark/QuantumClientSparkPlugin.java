package dev.ultreon.qvoxel.client.spark;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.gui.ChatMessage;
import dev.ultreon.qvoxel.client.gui.Overlays;
import dev.ultreon.qvoxel.spark.QuantumSparkPlugin;
import me.lucko.spark.common.platform.PlatformInfo;
import org.jetbrains.annotations.NotNull;

public class QuantumClientSparkPlugin extends QuantumSparkPlugin {
    @Override
    protected PlatformInfo.Type getType() {
        return PlatformInfo.Type.CLIENT;
    }

    @Override
    protected void sendMessage(@NotNull String serialize) {
        CommonConstants.LOGGER.info(serialize);
        Overlays.CHAT.addMessage(new ChatMessage(System.currentTimeMillis(), serialize, null));
    }
}
