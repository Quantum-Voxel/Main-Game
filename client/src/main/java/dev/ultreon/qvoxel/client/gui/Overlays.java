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

package dev.ultreon.qvoxel.client.gui;

import dev.ultreon.qvoxel.CommonConstants;

public class Overlays {
    public static final ChatOverlay CHAT = new ChatOverlay();
    public static final BuriedBlockOverlay BURIED_BLOCK = new BuriedBlockOverlay();
    public static final HotbarOverlay HOTBAR = new HotbarOverlay();
    public static final HealthOverlay HEALTH = new HealthOverlay();
    public static final HungerOverlay HUNGER = new HungerOverlay();
    public static final PlayerListOverlay PLAYER_LIST = new PlayerListOverlay();
    public static final NotificationOverlay NOTIFICATIONS = new NotificationOverlay();

    public static void init(OverlayManager overlayManager) {
        overlayManager.registerTop(CommonConstants.id("buried_block"), BURIED_BLOCK);
        overlayManager.registerTop(CommonConstants.id("chat"), CHAT);
        overlayManager.registerTop(CommonConstants.id("player_list"), PLAYER_LIST);
        overlayManager.registerTop(CommonConstants.id("hotbar"), HOTBAR);
        overlayManager.registerTop(CommonConstants.id("health"), HEALTH);
        overlayManager.registerTop(CommonConstants.id("hunger"), HUNGER);
        overlayManager.registerTop(CommonConstants.id("notifications"), NOTIFICATIONS);
    }
}
