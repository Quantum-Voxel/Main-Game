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

import dev.ultreon.qvoxel.CommonConstants;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

public class DeveloperMode {
    public static DevPipe devPipe = (tag, message) -> CommonConstants.LOGGER.trace(
            "[DEV] {}: {}", tag, message
    );
    public static boolean enabled;

    static {
        try {
            enabled = FabricLoader.getInstance().isDevelopmentEnvironment();
        } catch (RuntimeException e) {
            enabled = false;
        }
    }

    private static boolean enableDomainWarping = false;
    private static boolean enableImGui = false;
    private static boolean enableGuiDebug = false;

    public static boolean isDevFlagEnabled(DevFlag devFlag) {
        return enabled && switch (devFlag) {
            case DomainWarping -> enableDomainWarping;
            case ImGui -> enableImGui;
            case GuiDebug -> enableGuiDebug;
            default -> true;
        } && devFlag.isSupported();
    }

    public static void setDevFlagEnabled(DevFlag devFlag, boolean enabled) {
        if (!DeveloperMode.enabled) return;
        if (isDevFlagEnabled(devFlag) != enabled) {
            CommonConstants.LOGGER.info("Setting {} to {}", devFlag, enabled);
            switch (devFlag) {
                case DomainWarping -> enableDomainWarping = enabled;
                case ImGui -> {
                    enableImGui = enabled;
                    devPipe.send("ImGui", String.valueOf(enabled));
                }
                case GuiDebug -> enableGuiDebug = enabled;
                default -> CommonConstants.LOGGER.warn("Dev Flag {} not supported", devFlag);
            }
        }
    }

    public static @NotNull DevPipe getDevPipe() {
        return enabled ? devPipe : (_, _) -> {

        };
    }
}
