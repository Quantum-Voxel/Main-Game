package dev.ultreon.qvoxel.client.gui.widget;

import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.gui.ContainerWidget;
import dev.ultreon.qvoxel.client.render.Color;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11C.*;

public class ColorChooserWidget extends ContainerWidget {
    private final Color color = new Color();
    private final TextInputWidget redInput;
    private final TextInputWidget greenInput;
    private final TextInputWidget blueInput;
    private final float[] hsb = new float[3];
    private boolean huePressed;
    private boolean svPressed;

    public ColorChooserWidget() {
        this.redInput = addWidget(new TextInputWidget());
        this.greenInput = addWidget(new TextInputWidget());
        this.blueInput = addWidget(new TextInputWidget());

        this.redInput.setColor(Color.RED.copy().setBrightness(0.5f));
        this.greenInput.setColor(Color.GREEN.copy().setBrightness(0.5f));
        this.blueInput.setColor(Color.BLUE.copy().setBrightness(0.5f));

        java.awt.Color.RGBtoHSB((int) (color.r * 255), (int) (color.g * 255), (int) (color.b * 255), hsb);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        int minSize = Math.min(size.y, size.x);
        this.redInput.setPosition(minSize + 20, 9);
        this.redInput.resize(size.x - minSize - 28, 10);
        this.greenInput.setPosition(minSize + 20, 30);
        this.greenInput.resize(size.x - minSize - 28, 10);
        this.blueInput.setPosition(minSize + 20, 51);
        this.blueInput.resize(size.x - minSize - 28, 10);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
        updateColorRGB();
    }

    public void setColor(int argb) {
        this.color.setARGB(argb);
        this.color.a = 1;
        updateColorRGB();
    }

    public float getHue() {
        return hsb[0];
    }

    public void setHue(float hue) {
        this.hsb[0] = hue;
        updateColorHSB();
    }

    public float getSaturation() {
        return hsb[1];
    }

    public void setSaturation(float saturation) {
        this.hsb[1] = saturation;
        updateColorHSB();
    }

    public float getBrightness() {
        return hsb[2];
    }

    public void setBrightness(float brightness) {
        this.hsb[2] = brightness;
        updateColorHSB();
    }

    public void setColor(float red, float green, float blue) {
        this.color.set(red, green, blue, 1f);
    }

    public void setColor(float red, float green, float blue, float alpha) {
        this.color.set(red, green, blue, alpha);
    }

    @Override
    public void renderWidget(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        renderer.drawDisplay(0, 0, size.x, size.y);

        int minSize = Math.min(size.y, size.x);
        renderer.fillRect(4, 4, minSize - 8, minSize - 8, 0xff404040);

        var svShader = QuantumClient.get().shaders.getColorChooserSV();
        var hueShader = QuantumClient.get().shaders.getColorChooserHue();

        svShader.use();
        svShader.setUniform("hue", hsb[0]);
        if (minSize >= 20) {
            renderer.rect(5, 5, minSize - 10, minSize - 10, svShader);
        }
        glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ZERO);
        renderer.fillRect((int) (5 + hsb[1] * (minSize - 10)) - 2, (int) (4 + (1 - hsb[2]) * (minSize - 11)), 5, 1, 0xffffffff);
        renderer.fillRect((int) (5 + hsb[1] * (minSize - 10)), (int) (4 + (1 - hsb[2]) * (minSize - 11)) - 2, 1, 5, 0xffffffff);
        glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        renderer.fillRect(minSize - 1, 4, 12, minSize - 8, 0xff404040);
        hueShader.use();
        renderer.rect(minSize, 5, 10, minSize - 10, hueShader);
        glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ZERO);
        renderer.fillRect(minSize, (int) (5 + hsb[0] * (minSize - 11)), 10, 1, 0xffffffff);
        glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (huePressed) {
            int y = mouseY - 5;
            this.hsb[0] = Math.clamp(y / (minSize - 11f), 0, 1);
        }

        if (svPressed) {
            int x = mouseX - 5;
            int y = mouseY - 5;
            this.hsb[1] = Math.clamp(x / (minSize - 11f), 0, 1);
            this.hsb[2] = 1 - Math.clamp(y / (minSize - 11f), 0, 1);
        }

        if (huePressed || svPressed) {
            updateColorHSB();
        }
    }

    private void updateColorHSB() {
        color.setARGB(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) | 0xFF000000);
        redInput.setValue(String.valueOf((int)(color.r * 255)));
        greenInput.setValue(String.valueOf((int)(color.g * 255)));
        blueInput.setValue(String.valueOf((int)(color.b * 255)));
    }

    private void updateColorRGB() {
        java.awt.Color.RGBtoHSB((int) (color.r * 255), (int) (color.g * 255), (int) (color.b * 255), hsb);
        redInput.setValue(String.valueOf((int)(color.r * 255)));
        greenInput.setValue(String.valueOf((int)(color.g * 255)));
        blueInput.setValue(String.valueOf((int)(color.b * 255)));
    }

    @Override
    public boolean onMouseButtonPress(int mouseX, int mouseY, int button, int mods) {
        if (super.onMouseButtonPress(mouseX, mouseY, button, mods)) {
            return false;
        }

        int minSize = Math.min(size.y, size.x);
        huePressed = isPosWithin(mouseX, mouseY, minSize, 5, 10, minSize - 10);
        svPressed = isPosWithin(mouseX, mouseY, 5, 5, minSize - 10, minSize - 10);
        return true;
    }

    @Override
    public void onMouseButtonRelease(int mouseX, int mouseY, int button, int mods) {
        super.onMouseButtonRelease(mouseX, mouseY, button, mods);

        huePressed = false;
        svPressed = false;
    }

    private boolean isPosWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }
}
