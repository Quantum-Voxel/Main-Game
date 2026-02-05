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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.SanityCheck;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.gui.font.Glyph;
import dev.ultreon.qvoxel.client.render.*;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.resource.Resource;
import dev.ultreon.qvoxel.text.MutableText;
import dev.ultreon.qvoxel.text.Style;
import dev.ultreon.qvoxel.text.Text;
import dev.ultreon.qvoxel.util.ResourceNotFoundException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBTruetype.*;

/**
 * A class responsible for rendering fonts in a 2D OpenGL context. This class loads TrueType font files,
 * creates a bitmap for the glyphs, and manages the rendering of text using the baked font data.
 * It handles texture generation and uploads the processed font data for graphical rendering.
 */
@SuppressWarnings("SuspiciousNameCombination")
public class FontRenderer extends GameNode implements GLObject {
    private static final int BITMAP_W = 2048;
    private static final int BITMAP_H = 2048;
    public static final int FIRST_CHAR = 0x0020;
    public static final int MAX_CHAR = 0x20000;

    private final int texID;
    private final STBTTBakedChar.Buffer charData;
    public final int lineHeight;
    private final STBTTFontinfo fontInfo;
    private final Color tmpColor = new Color();

    /**
     * Constructs a new FontRenderer instance for rendering text using a specified font.
     * This class handles loading font data, creating a bitmap for the glyphs, and uploading it as a texture for rendering.
     *
     * @param identifier The identifier for the font resource, used to locate the TTF file.
     * @param fontSize   The size of the font to render.
     * @param lineHeight The height of a line of text when rendered.
     * @throws ResourceNotFoundException If the font resource cannot be found or loaded.
     * @throws RuntimeException          If the font initialization, baking, or texture creation fails.
     */
    public FontRenderer(Identifier identifier, float fontSize, int lineHeight) {
        // Load TTF file into ByteBuffer
        @Nullable Resource resource = QuantumClient.get().resourceManager.getResource(identifier);
        if (resource == null) {
            throw new ResourceNotFoundException(identifier);
        }
        byte[] array = resource.readBytes();
        if (array == null) {
            throw new ResourceNotFoundException(identifier);
        }
        ByteBuffer ttf = ByteBuffer.allocateDirect(array.length);
        ttf.put(array);
        ttf.flip();

        // Allocate buffer for char data (ASCII 32–MAX_CHAR)
        charData = STBTTBakedChar.malloc(MAX_CHAR - FIRST_CHAR);

        // Allocate bitmap for glyphs
        ByteBuffer bitmap = ByteBuffer.allocateDirect(BITMAP_W * BITMAP_H);

        // Initialize font info
        fontInfo = STBTTFontinfo.malloc();
        if (!stbtt_InitFont(fontInfo, ttf)) {
            throw new RuntimeException("Failed to initialize font: Could not find font info");
        }

        // Bake font bitmap
        int i = stbtt_BakeFontBitmap(ttf, fontSize, bitmap, BITMAP_W, BITMAP_H, FIRST_CHAR, charData);
        if (i == 0) {
            throw new RuntimeException("Failed to bake font bitmap: No characters were baked");
        }

        // Convert the grayscale result to monochrome:
        for (int idx = 0; idx < bitmap.capacity(); idx++) {
            bitmap.put(idx, (bitmap.get(idx) & 0xFF) >= 1 ? (byte) 255 : (byte) 0);
        }

        // Upload as OpenGL texture
        texID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texID);

        // Ensure single-byte rows are handled correctly
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        // Use core-profile compatible single-channel texture format
        // Internal format: GL_R8, format: GL_RED
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, BITMAP_W, BITMAP_H, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);

        // Swizzle so shaders sampling RGBA get (1,1,1,coverage)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_ONE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_ONE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_ONE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_RED);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Restore default alignment (optional)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        int error = glGetError();
        if (error != GL_NO_ERROR) {
            throw new RuntimeException("Failed to create font texture: %s".formatted(GLUtils.getErrorName(error)));
        }

        this.lineHeight = lineHeight;
    }

    /**
     * Renders text on the GUI at the specified position using the default color (white).
     * NOTE: Internal, use {@link GuiRenderer#drawString(String, int, int)} or similar methods instead
     *
     * @param renderer The GUI renderer responsible for drawing the text.
     * @param text     The text string to be rendered.
     * @param x        The x-coordinate (horizontal position) at which to render the text.
     * @param y        The y-coordinate (vertical position) at which to render the text.
     */
    @ApiStatus.Internal
    public void renderText(GuiRenderer renderer, String text, float x, float y) {
        renderText(renderer, text, x, y, Color.WHITE);
    }

    /**
     * Renders a string of text on the GUI at the specified position with a specified color.
     * This method is internal and is used for rendering text with fine-grained control.
     * Use {@link GuiRenderer#drawString(String, int, int, int)} or similar methods instead.
     *
     * @param renderer The GUI renderer responsible for managing the rendering process.
     * @param text     The string of text to be rendered on the GUI.
     * @param x        The horizontal position (x-coordinate) to start rendering the text.
     * @param y        The vertical position (y-coordinate) to start rendering the text.
     * @param color    The color to apply to the rendered text, including its transparency (alpha value).
     */
    @ApiStatus.Internal
    public void renderText(GuiRenderer renderer, String text, float x, float y, Color color) {
        if (color.a <= 0) color.a = 1;

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texID);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xb = stack.floats(x);
            FloatBuffer yb = stack.floats(y);

            Tessellator tessellator = renderer.getTessellator();
            tessellator.begin(GLShape.Quads, VertexAttributes.POSITION, VertexAttributes.UV);

            bakeString(x, text, xb, yb, stack, tessellator, Style.EMPTY);

            tessellator.draw(renderer, color);

            renderer.debugDraw(new DebugDraw((int) x, (int) y, (int) (xb.get(0) - x), (int) (yb.get(0) - y - lineHeight), 0x4000FF00, true));
        }
    }

    /**
     * Renders a block of text on the GUI at the specified position with a specified color.
     * This method handles text rendering with proper styles and manages the rendering state.
     * Use {@link GuiRenderer#drawString(Text, int, int, int)} or similar methods instead.
     *
     * @param renderer The GUI renderer responsible for managing the rendering process.
     * @param text The text object containing the content and style to be rendered.
     * @param x The horizontal position (x-coordinate) to start rendering the text.
     * @param y The vertical position (y-coordinate) to start rendering the text.
     * @param color The color to apply to the rendered text, including its transparency (alpha value).
     */
    public void renderText(GuiRenderer renderer, Text text, float x, float y, Color color) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texID);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


        Tessellator tessellator = renderer.getTessellator();
        tessellator.begin(GLShape.Quads, VertexAttributes.POSITION, VertexAttributes.UV, VertexAttributes.STATE);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xb = stack.floats(x);
            FloatBuffer yb = stack.floats(y);
            if (text instanceof MutableText iteratingText) {
                for (Text textElement : iteratingText) {
                    if (textElement instanceof MutableText)
                        throw new SanityCheck("Nested mutable text shouldn't be happening...");

                    String text1 = textElement.getText();
                    bakeString(x, text1, xb, yb, stack, tessellator, textElement.getStyle());
                }
            }

            renderer.debugDraw(new DebugDraw((int) x, (int) y, (int) (xb.get(0) - x), (int) (yb.get(0) - y + lineHeight), 0x4000FF00, true));
        }

        tessellator.draw(renderer, color);
    }

    private void bakeString(float x, String str, FloatBuffer xb, FloatBuffer yb, MemoryStack stack, Tessellator tessellator, Style style) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            bakeChar(x, c, xb, yb, stack, tessellator, style);
        }
    }

    private void bakeChar(float x, char c, FloatBuffer xb, FloatBuffer yb, MemoryStack stack, Tessellator tessellator, Style style) {
        if (c == '\n') {
            // Reset pen X to start and move pen Y down by lineHeight
            xb.put(0, x);
            yb.put(0, yb.get(0) + lineHeight);
            return;
        }
        if (c == ' ') {
            xb.put(0, xb.get(0) + 4);
            return;
        }
        if (c < FIRST_CHAR) return;

        STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
        // stbtt_GetBakedQuad uses xb/yb as pen relativePos and advances them
        stbtt_GetBakedQuad(charData, BITMAP_W, BITMAP_H, c - FIRST_CHAR, xb, yb, q, true);

        float x0 = q.x0();
        float x1 = q.x1();
        if (style.isItalic()) {
            x0 += 3;
            x1 += 3;
        }

        // Use screen-space positions from q (NOT bc.x0/y0/x1/y1)
        tessellator.addVertex(x0, q.y0(), 0).setUV(q.s0(), q.t0()).setState(0).endVertex();
        tessellator.addVertex(x1, q.y0(), 0).setUV(q.s1(), q.t0()).setState(0).endVertex();
        tessellator.addVertex(x1, q.y1(), 0).setUV(q.s1(), q.t1()).setState(0).endVertex();
        tessellator.addVertex(x0, q.y1(), 0).setUV(q.s0(), q.t1()).setState(0).endVertex();

        if (style.isBold()) {
            tessellator.addVertex(x0 + 1, q.y0(), 0).setUV(q.s0(), q.t0()).setState(0).endVertex();
            tessellator.addVertex(x1 + 1, q.y0(), 0).setUV(q.s1(), q.t0()).setState(0).endVertex();
            tessellator.addVertex(x1 + 1, q.y1(), 0).setUV(q.s1(), q.t1()).setState(0).endVertex();
            tessellator.addVertex(x0 + 1, q.y1(), 0).setUV(q.s0(), q.t1()).setState(0).endVertex();

            xb.put(0, xb.get(0) + 1);
        }

        if (style.isUnderlined()) {
            tessellator.addVertex(x0, q.y0() + lineHeight, 0).setUV(q.s0(), q.t0()).setState(1).endVertex();
            tessellator.addVertex(x1, q.y0() + lineHeight, 0).setUV(q.s1(), q.t0()).setState(1).endVertex();
            tessellator.addVertex(x1, q.y0() + lineHeight + 1, 0).setUV(q.s1(), q.t1()).setState(1).endVertex();
            tessellator.addVertex(x0, q.y0() + lineHeight + 1, 0).setUV(q.s0(), q.t1()).setState(1).endVertex();
        }
        if (style.isStrikethrough()) {
            tessellator.addVertex(x0, q.y0() + (int) (lineHeight / 2f), 0).setUV(q.s0(), q.t0()).setState(1).endVertex();
            tessellator.addVertex(x1, q.y0() + (int) (lineHeight / 2f), 0).setUV(q.s1(), q.t0()).setState(1).endVertex();
            tessellator.addVertex(x1, q.y0() + (int) (lineHeight / 2f) - 1, 0).setUV(q.s1(), q.t1()).setState(1).endVertex();
            tessellator.addVertex(x0, q.y0() + (int) (lineHeight / 2f) - 1, 0).setUV(q.s0(), q.t1()).setState(1).endVertex();
        }
    }

    public void renderTextWithShadow(GuiRenderer renderer, String text, float x, float y, Color color) {
        renderText(renderer, text, x + (QuantumClient.get().diagonalFontShadow ? 1 : 0), y + 1, tmpColor.set(color).mulRgb(0.4f));
        renderText(renderer, text, x, y, color);
    }

    public void renderTextWithShadow(GuiRenderer renderer, Text text, float x, float y, Color color) {
        Tessellator tessellator = renderer.getTessellator();
        tessellator.begin(GLShape.Quads, VertexAttributes.POSITION, VertexAttributes.UV, VertexAttributes.STATE);
        renderText(renderer, text, x + (QuantumClient.get().diagonalFontShadow ? 1 : 0), y + 1, tmpColor.set(color).mulRgb(0.4f));
        renderText(renderer, text, x, y, color);
    }

    public int widthOf(String text) {
        int width = 0;
        int maxWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                width += 4;
                continue;
            }
            if (c == '\n') {
                maxWidth = Math.max(maxWidth, width);

                // Reset pen X to start and move pen Y down by lineHeight
                width = 0;
                continue;
            }
            if (c < ' ')
                continue;
            width += (int) charData.get(c - FIRST_CHAR).xadvance();
        }

        maxWidth = Math.max(maxWidth, width);
        return maxWidth;
    }

    public int heightOf(String text) {
        int height = lineHeight;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                // Reset pen X to start and move pen Y down by lineHeight
                height += lineHeight;
                continue;
            }
        }

        return height;
    }

    public Glyph getGlyph(char c, Glyph out) {
        return out.set(charData.get(c - FIRST_CHAR));
    }

    @Override
    public int getObjectId() {
        return texID;
    }

    @Override
    public void delete() {
        glDeleteTextures(texID);
        charData.free();
        fontInfo.free();
    }

    public int widthOf(Text text) {
        int width = 0;
        if (text instanceof MutableText iteratingText) {
            for (Text textElement : iteratingText) {
                if (textElement instanceof MutableText)
                    throw new SanityCheck("Nested mutable text shouldn't be happening...");

                String text1 = textElement.getText();
                width += widthOf(text1);
                if (textElement.getStyle().isBold())
                    width += text1.length();
            }
            return width;
        }

        width = widthOf(text.getText());
        if (text.getStyle().isBold()) width += text.getText().length();
        return width;
    }

    public int widthOf(char c) {
        return (int) charData.get(c - FIRST_CHAR).xadvance();
    }
}
