package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.libs.translations.v1.Language;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.gui.widget.TextButtonWidget;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.network.packets.c2s.C2SRespawnPacket;

public class DeathScreen extends Screen {
    private final ClientPlayerEntity diedPlayer;
    private TextButtonWidget respawnButton;

    public DeathScreen(ClientPlayerEntity diedPlayer) {
        super(Language.translate("quantum.screen.death"));
        this.diedPlayer = diedPlayer;
    }

    @Override
    public void init() {
        this.respawnButton = addWidget(new TextButtonWidget(Language.translate("quantum.screen.death.respawn")));
        this.respawnButton.setCallback(() -> {
            client.clearGuiLayers();
            client.getWindow().captureMouse(); // Needed for clearing GUI

            diedPlayer.getWorldRenderer().clearChunks();
            diedPlayer.connection.send(new C2SRespawnPacket());
        });
    }

    @Override
    public void resized() {
        this.respawnButton.resize(100, 20);
        this.respawnButton.setPosition(size.x / 2 - 50, size.y - 30);
    }

    public ClientPlayerEntity getDiedPlayer() {
        return diedPlayer;
    }

    public TextButtonWidget getRespawnButton() {
        return respawnButton;
    }
}
