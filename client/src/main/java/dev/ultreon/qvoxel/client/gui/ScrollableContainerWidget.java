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

import dev.ultreon.qvoxel.client.render.GuiRenderer;
import org.joml.Vector2f;

public abstract class ScrollableContainerWidget extends ContainerWidget {
    public final Vector2f scroll = new Vector2f();

    protected ScrollableContainerWidget() {
        super();
    }

    protected void renderChildren(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        renderer.pushMatrix();
        renderer.translate(-scroll.x, -scroll.y, 0);
        for (Widget child : widgets) {
            renderer.pushMatrix();
            renderer.translate((float) -child.position.x, (float) -child.position.y, (float) -child.position.z);
            renderChild(renderer, child, (int) (mouseX - child.position.x), (int) (mouseY - child.position.y), partialTicks);
            renderer.popMatrix();
        }
        renderer.popMatrix();
    }
}
