package dev.ultreon.qvoxel.client.gui;

import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationOverlay extends Overlay {
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public void add(String title, String message) {
        notifications.add(new Notification(title, message));
    }

    @Override
    public void render(ClientPlayerEntity player, GuiRenderer renderer, float partialTick) {
        int y = 0;
        for (Notification notification : List.copyOf(notifications)) {
            if (notification.created() + 5000 > System.currentTimeMillis()) {
                notifications.remove(notification);
                continue;
            }

            y = notification.render(renderer, y);
        }
    }
}
