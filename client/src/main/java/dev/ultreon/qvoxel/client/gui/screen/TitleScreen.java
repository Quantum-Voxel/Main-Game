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

package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.libs.translations.v1.Language;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.debug.ClientDebugging;
import dev.ultreon.qvoxel.client.gui.widget.TextButtonWidget;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.texture.Texture;
import dev.ultreon.qvoxel.network.system.DeveloperMode;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.sdl.SDLKeycode;

public class TitleScreen extends Screen {
    private static final String COPYRIGHT = "© Copyright 2025, Ultreon Studios";
    public static final Identifier TITLE_TEXTURE = CommonConstants.id("textures/gui/quantum_voxel.png");
    public static final int INTERNAL_MODS = 4;
    private final String rawGameVersion;
    private TextButtonWidget createWorldBtn;
    private TextButtonWidget multiplayerBtn;
    private TextButtonWidget exitBtn;

    public TitleScreen() {
        super(Language.translate("quantum.screen.title"));
        String rawGameVersion1;
        try {
            rawGameVersion1 = FabricLoader.getInstance().getRawGameVersion();
        } catch (RuntimeException e) {
            rawGameVersion1 = "UNKNOWN";
        }
        rawGameVersion = rawGameVersion1;
    }

    @Override
    public void init() {
        createWorldBtn = addWidget(new TextButtonWidget(Language.translate("quantum.screen.title.singleplayer")));
        createWorldBtn.setCallback(() -> client.showScreen(new WorldSelectionScreen()));
        if (DeveloperMode.enabled) {
            multiplayerBtn = addWidget(new TextButtonWidget(Language.translate("quantum.screen.title.multiplayer.dev")));
            multiplayerBtn.setCallback(() -> client.connectToServer(client.isKeyDown(SDLKeycode.SDLK_LSHIFT) ? "wss://quantumsrv.ultreon.dev:443/" : "ws://localhost:38800/"));
        }
        exitBtn = addWidget(new TextButtonWidget(Language.translate("quantum.ui.exit")));
        exitBtn.setCallback(client::onWindowClose);
    }

    @Override
    public void resized() {
        if (DeveloperMode.enabled) {
            createWorldBtn.resize(100, 40);
            createWorldBtn.setPosition(size.x / 2 - 50, size.y / 2 - 20);
            multiplayerBtn.resize(100, 20);
            multiplayerBtn.setPosition(size.x / 2 - 50, size.y / 2 + 18);
            exitBtn.resize(100, 20);
            exitBtn.setPosition(size.x / 2 - 50, size.y / 2 + 36);
        } else {
            createWorldBtn.resize(100, 40);
            createWorldBtn.setPosition(size.x / 2 - 50, size.y / 2 - 20);
            exitBtn.resize(100, 20);
            exitBtn.setPosition(size.x / 2 - 50, size.y / 2 + 18);
        }
    }

    @Override
    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(renderer, mouseX, mouseY, partialTicks);

        renderer.drawTexture(TITLE_TEXTURE, size.x / 2 - 109, 30, 219, 75);
        String message = "Quantum Voxel v" + rawGameVersion;
        int modCount = FabricLoader.getInstance().getAllMods().size() - INTERNAL_MODS;
        if (modCount < 0) {
            message += " (" + modCount + " mods loaded, wait what?)";
        } else if (modCount == 1) {
            message += " (" + modCount + " mod loaded)";
        } else if (modCount == 67) {
            message += " (" + modCount + " mods loaded, noooo)";
        } else if (modCount == 69) {
            message += " (" + modCount + " mods loaded, nice :D)";
        } else {
            message += " (" + modCount + " mods loaded)";
        }
        renderer.drawString(message, 10, size.y - 10);

        renderer.drawString(COPYRIGHT, size.x - 10 - client.font.widthOf(COPYRIGHT), size.y - 10);

        Texture pasteTexture = ClientDebugging.getPasteTexture();
        if (pasteTexture != null) {
            int width = ClientDebugging.getPasteTexture().getWidth();
            int height = ClientDebugging.getPasteTexture().getHeight();
            GL11.glDisable(GL11.GL_BLEND);
            renderer.drawTexture(pasteTexture, 0, 0, width, height, 0, 0, width, height, width, height);
            GL11.glEnable(GL11.GL_BLEND);
        }
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
