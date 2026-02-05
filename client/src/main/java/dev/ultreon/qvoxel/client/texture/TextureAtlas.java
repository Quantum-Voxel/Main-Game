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

package dev.ultreon.qvoxel.client.texture;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.render.GLUtils;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL32;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL30.*;

public class TextureAtlas extends Texture {
    private final AtlasRegion defaultRegion;
    private final GuiRenderer renderer;
    private int atlasTexture;
    private int fbo;
    private int width, height;
    private int cursorX, cursorY, rowHeight;

    private final Map<Identifier, AtlasRegion> regions = new HashMap<>();

    public TextureAtlas(int initialSize) {
        super(-1, TextureFormat.RGBA8);
        width = initialSize;
        height = initialSize;
        cursorX = 0;
        cursorY = 0;
        rowHeight = 0;

        createAtlasTexture();
        createFBO();

        renderer = new GuiRenderer(null);

        defaultRegion = addTexture(CommonConstants.id("default"), 16, 16, painter -> {
            painter.fillColor(0xFFFFB000);
            painter.fillRect(0, 0, 8, 8, 0xFF202020);
            painter.fillRect(8, 8, 8, 8, 0xFF202020);
        });
    }

    private void createAtlasTexture() {
        atlasTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, atlasTexture);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, 0);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    private void createFBO() {
        fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D, atlasTexture, 0);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Failed to create FBO for atlas!");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public AtlasRegion addTexture(Identifier location, int width, int height, Consumer<Painter> painter) {
        Painter p = new Painter(width, height);
        painter.accept(p);

        renderer.resize(this.width, this.height);

        // Upload image into atlas via FBO
        glViewport(0, 0, this.width, this.height);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        Texture texture = p.createTexture();
        int yInAtlas = cursorY;

        renderer.drawTexture(texture, cursorX + 1, this.height - yInAtlas - 1, width, -height);

        // Add border
        // Left
        renderer.drawTexture(texture, cursorX, this.height - yInAtlas - height - 1, 1, height, width - 1, 0, 1, height);

        // Top
        renderer.drawTexture(texture, cursorX + 1, this.height - yInAtlas - 1, width, 1, 0, 0, width, 1);

        // Right
        renderer.drawTexture(texture, cursorX + width + 1, this.height - yInAtlas - height - 1, 1, height, 0, 0, 1, height);

        // Bottom
        renderer.drawTexture(texture, cursorX + 1, this.height - yInAtlas - height - 2, width, 1, 0, height - 1, width, 1);

        // Corners
        renderer.drawTexture(texture, cursorX, this.height - yInAtlas - height - 2, 1, 1, 0, height - 1, 1, 1);
        renderer.drawTexture(texture, cursorX + width + 1, this.height - yInAtlas - height - 2, 1, 1, width - 1, height - 1, 1, 1);
        renderer.drawTexture(texture, cursorX, this.height - yInAtlas - 1, 1, 1, 0, 0, 1, 1);
        renderer.drawTexture(texture, cursorX + width + 1, this.height - yInAtlas - 1, 1, 1, width - 1, 0, 1, 1);

        texture.delete();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, QuantumClient.get().getWindow().getWidth(), QuantumClient.get().getWindow().getHeight());

        AtlasRegion region = new AtlasRegion(cursorX + 1, yInAtlas + 1, width, height, this);
        regions.put(location, region);

        cursorX += width + 2;
        rowHeight = Math.max(rowHeight, height + 2);

        return region;
    }


    public AtlasRegion addTexture(Identifier location) {
        Texture texture;
        if (location.equals(CommonConstants.id("textures/default.png"))) {
            regions.put(location, defaultRegion);
            return defaultRegion;
        }
        try {
            texture = new Texture(location);
        } catch (IOException | TextureException e) {
            CommonConstants.LOGGER.error("Failed to load texture {}", location, e);
            regions.put(location, defaultRegion);
            return defaultRegion;
        }
        int imgWidth = texture.getWidth();
        int imgHeight = texture.getHeight();

        // Check if it fits in current row
        if (cursorX + imgWidth > width) {
            cursorX = 0;
            cursorY += rowHeight;
            rowHeight = 0;
        }

        // Check if we need resize
        if (cursorY + imgHeight > height) {
            resizeAtlas();
            return addTexture(location); // Retry after resize
        }

        // Upload image into atlas via FBO
        glViewport(0, 0, width, height);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        int yInAtlas = cursorY;
        renderer.drawTexture(texture, cursorX + 1, height - yInAtlas - 1, imgWidth, -imgHeight);

        // Add border
        // Left
        renderer.drawTexture(texture, cursorX, height - yInAtlas - imgHeight - 1, 1, imgHeight, 0, 0, 1, -imgHeight);

        // Top
        renderer.drawTexture(texture, cursorX + 1, height - yInAtlas - 1, imgWidth, 1, 0, 0, imgWidth, 1);

        // Right
        renderer.drawTexture(texture, cursorX + imgWidth + 1, height - yInAtlas - imgHeight - 1, 1, imgHeight, imgWidth - 1, imgHeight, 1, -imgHeight);

        // Bottom
        renderer.drawTexture(texture, cursorX + 1, height - yInAtlas - imgHeight - 2, imgWidth, 1, 0, imgHeight - 1, imgWidth, 1);

        // Corners
        renderer.drawTexture(texture, cursorX, height - yInAtlas - imgHeight - 2, 1, 1, 0, imgHeight - 1, 1, 1);
        renderer.drawTexture(texture, cursorX + imgWidth + 1, height - yInAtlas - imgHeight - 2, 1, 1, imgWidth - 1, imgHeight - 1, 1, 1);
        renderer.drawTexture(texture, cursorX, height - yInAtlas - 1, 1, 1, 0, 0, 1, 1);
        renderer.drawTexture(texture, cursorX + imgWidth + 1, height - yInAtlas - 1, 1, 1, imgWidth - 1, 0, 1, 1);

        texture.delete();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, QuantumClient.get().getWindow().getWidth(), QuantumClient.get().getWindow().getHeight());

        AtlasRegion region = new AtlasRegion(cursorX + 1, yInAtlas + 1, imgWidth, imgHeight, this);
        regions.put(location.mapPath(path -> {
            path = "textures/" + path;
            if (!path.endsWith(".png")) return path + ".png";
            return path;
        }), region);

        // Advance packing cursor
        cursorX += imgWidth + 2;
        rowHeight = Math.max(rowHeight, imgHeight + 2);

        return region;
    }

    private void resizeAtlas() {
        int newWidth = width * 2;
        int newHeight = height * 2;

        int newTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, newTex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, newWidth, newHeight, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        int error = glGetError();
        if (error != GL_NO_ERROR) {
            throw new RuntimeException("Failed to resize atlas texture: " + GLUtils.getErrorName(error));
        }

        // Copy old contents into new texture
        glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
        int blitFBO = glGenFramebuffers();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, blitFBO);
        glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D, newTex, 0);

        error = glGetError();
        if (error != GL_NO_ERROR) {
            throw new RuntimeException("Failed to create blit FBO for atlas resize: " + GLUtils.getErrorName(error));
        }

        glBlitFramebuffer(0, 0, width, height,
                0, 0, width, height,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);

        error = glGetError();
        if (error != GL_NO_ERROR) {
            throw new RuntimeException("Failed to blit atlas texture: " + GLUtils.getErrorName(error));
        }

        // Cleanup old atlas
        glDeleteFramebuffers(fbo);
        glDeleteTextures(atlasTexture);

        error = glGetError();
        if (error != GL_NO_ERROR) {
            throw new RuntimeException("Failed to cleanup old atlas: " + GLUtils.getErrorName(error));
        }

        // Replace with new
        atlasTexture = newTex;
        fbo = blitFBO;
        width = newWidth;
        height = newHeight;
    }

    public int getAtlasTexture() {
        return atlasTexture;
    }

    public void dump(Path path) {
        try (OutputStream out = Files.newOutputStream(path)) {
            GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, fbo);
            ByteBuffer outBuffer = BufferUtils.createByteBuffer(width * height * 4);
            GL32.glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_BYTE, outBuffer);
            writePng(out, outBuffer, width, height);
            GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, 0);
            out.flush();
            int error = glGetError();
            if (error != GL_NO_ERROR) {
                throw new RuntimeException("Failed to dump atlas texture: " + GLUtils.getErrorName(error));
            }
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to dump atlas texture", e);
        }
    }

    private void writePng(OutputStream out, ByteBuffer outBuffer, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        outBuffer.asIntBuffer().get(pixels);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        ImageIO.write(image, "png", out);
    }

    public Collection<AtlasRegion> getRegions() {
        return regions.values();
    }

    public AtlasRegion getRegion(Identifier location) {
        Identifier mapped = location.mapPath(path -> {
            path = "textures/" + path;
            if (!path.endsWith(".png")) return path + ".png";
            return path;
        });
        AtlasRegion atlasRegion = regions.get(mapped);
        if (atlasRegion == null) {
            atlasRegion = defaultRegion;
            regions.put(mapped, atlasRegion);
            CommonConstants.LOGGER.warn("Failed to find texture {} in atlas {}", location, this);
        }
        return atlasRegion;
    }

    public AtlasRegion getDefaultRegion() {
        return defaultRegion;
    }

    public void use() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTexture);
    }

    public void delete() {
        glDeleteTextures(atlasTexture);
        glDeleteFramebuffers(fbo);
    }

    public Vector2f getSize() {
        return new Vector2f(width, height);
    }

    public Collection<AtlasRegion> getTextures() {
        return regions.values();
    }

    public static final class AtlasRegion implements TextureRegion {
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final TextureAtlas textureAtlas;

        private final Vector4f uvTransform = new Vector4f();

        public AtlasRegion(float x, float y, float width, float height,
                           TextureAtlas textureAtlas) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.textureAtlas = textureAtlas;

            uvTransform.set(getU(), getV(), getU2() - getU(), getV2() - getV());
        }

        public float getU() {
            return x / textureAtlas.width;
        }

        public float getV() {
            return y / textureAtlas.height;
        }

        public float getU2() {
            return (x + width) / textureAtlas.width;
        }

        public float getV2() {
            return (y + height) / textureAtlas.height;
        }

        public Vector4f getUvTransform() {
            return uvTransform;
        }

        public Vector4f getUvTransformRandom() {
            return uvTransform;
        }

        @Override
        public float x() {
            return x;
        }

        @Override
        public float y() {
            return y;
        }

        @Override
        public float width() {
            return width;
        }

        @Override
        public float height() {
            return height;
        }

        @Override
        public Texture texture() {
            return textureAtlas;
        }

        public TextureAtlas textureAtlas() {
            return textureAtlas;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != getClass()) return false;
            var that = (AtlasRegion) obj;
            return Float.floatToIntBits(x) == Float.floatToIntBits(that.x) &&
                    Float.floatToIntBits(y) == Float.floatToIntBits(that.y) &&
                    Float.floatToIntBits(width) == Float.floatToIntBits(that.width) &&
                    Float.floatToIntBits(height) == Float.floatToIntBits(that.height) &&
                    Objects.equals(textureAtlas, that.textureAtlas);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, width, height, textureAtlas);
        }

        @Override
        public String toString() {
            return "AtlasRegion[" +
                    "x=" + x + ", " +
                    "y=" + y + ", " +
                    "width=" + width + ", " +
                    "height=" + height + ", " +
                    "textureAtlas=" + textureAtlas + ']';
        }

    }

    @Override
    public int getObjectId() {
        return atlasTexture;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
