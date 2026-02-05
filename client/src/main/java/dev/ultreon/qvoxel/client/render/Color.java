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

package dev.ultreon.qvoxel.client.render;

public class Color {
    public static final Color WHITE = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    public static final Color BLACK = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    public static final Color RED = new Color(1.0f, 0.0f, 0.0f, 1.0f);
    public static final Color GREEN = new Color(0.0f, 1.0f, 0.0f, 1.0f);
    public static final Color BLUE = new Color(0.0f, 0.0f, 1.0f, 1.0f);
    public static final Color YELLOW = new Color(1.0f, 1.0f, 0.0f, 1.0f);
    public static final Color CYAN = new Color(0.0f, 1.0f, 1.0f, 1.0f);
    public static final Color MAGENTA = new Color(1.0f, 0.0f, 1.0f, 1.0f);
    public static final Color TRANSPARENT = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    public static final Color TRANSLUCENT = new Color(0.0f, 0.0f, 0.0f, 0.5f);
    public static final Color LIGHT_GRAY = new Color(0.75f, 0.75f, 0.75f, 1.0f);
    public static final Color GRAY = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    public static final Color DARK_GRAY = new Color(0.25f, 0.25f, 0.25f, 1.0f);
    public static final Color LIGHT_RED = new Color(1.0f, 0.5f, 0.5f, 1.0f);
    public static final Color LIGHT_GREEN = new Color(0.5f, 1.0f, 0.5f, 1.0f);
    public static final Color LIGHT_BLUE = new Color(0.5f, 0.5f, 1.0f, 1.0f);
    public static final Color LIGHT_YELLOW = new Color(1.0f, 1.0f, 0.5f, 1.0f);
    public static final Color LIGHT_CYAN = new Color(0.5f, 1.0f, 1.0f, 1.0f);
    public static final Color LIGHT_MAGENTA = new Color(1.0f, 0.5f, 1.0f, 1.0f);

    public float r;
    public float g;
    public float b;
    public float a;

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }

    public Color(int color) {
        setARGB(color);
    }

    public Color() {
        this(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public Color(Color topColor) {
        r = topColor.r;
        g = topColor.g;
        b = topColor.b;
        a = topColor.a;
    }

    public Color setARGB(int color) {
        r = (color >> 16 & 0xFF) / 255.0f;
        g = (color >> 8 & 0xFF) / 255.0f;
        b = (color & 0xFF) / 255.0f;
        a = (color >> 24 & 0xFF) / 255.0f;
        if (a < 0.0f) a = 1.0f;

        return this;
    }

    public int toARGB() {
        return (int) (a * 255) << 24 | (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
    }

    public Color set(Color color) {
        r = color.r;
        g = color.g;
        b = color.b;
        a = color.a;
        return this;
    }

    @Override
    public String toString() {
        return "#%02X%02X%02X%02X".formatted((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }

    public Color set(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    public Color mul(float scalar) {
        r *= scalar;
        g *= scalar;
        b *= scalar;
        a *= scalar;
        return this;
    }

    public Color mulRgb(float scalar) {
        r *= scalar;
        g *= scalar;
        b *= scalar;
        return this;
    }

    public Color invMul(float v) {
        r = (r + v) / (1.0f + v);
        g = (g + v) / (1.0f + v);
        b = (b + v) / (1.0f + v);
        return this;
    }

    public Color clampBrightness(float min, float max) {
        float brightness = getBrightness();
        if (brightness < min) {
            setBrightness(min);
        } else if (brightness > max) {
            setBrightness(max);
        }
        return this;
    }

    public Color normalizeBrightness(float min, float max) {
        float brightness = 0.2126f * r + 0.7152f * g + 0.0722f * b;
        float target = (min + max) * 0.5f; // aim for midpoint brightness
        float scale = target / brightness;

        r = Math.min(r * scale, 1f);
        g = Math.min(g * scale, 1f);
        b = Math.min(b * scale, 1f);

        return this;
    }

    public Color clampRgb(float min, float max) {
        r = Math.min(Math.max(r, min), max);
        g = Math.min(Math.max(g, min), max);
        b = Math.min(Math.max(b, min), max);
        return this;
    }

    public Color clamp(float min, float max) {
        r = Math.min(Math.max(r, min), max);
        g = Math.min(Math.max(g, min), max);
        b = Math.min(Math.max(b, min), max);
        a = Math.min(Math.max(a, min), max);
        return this;
    }

    public Color brighten(float factor) {
        // factor > 1 => brighter, factor < 1 => darker
        // Works linearly toward white (1) or black (0)

        if (factor > 1f) {
            // Brighten toward white
            float amount = factor - 1f;
            r += (1f - r) * amount;
            g += (1f - g) * amount;
            b += (1f - b) * amount;
        } else if (factor < 1f) {
            // Darken toward black
            r *= factor;
            g *= factor;
            b *= factor;
        }

        // Clamp values
        r = Math.min(Math.max(r, 0f), 1f);
        g = Math.min(Math.max(g, 0f), 1f);
        b = Math.min(Math.max(b, 0f), 1f);

        return this;
    }

    public Color setBrightness(float target) {
        // Clamp target brightness
        target = Math.max(0f, Math.min(1f, target));

        // Calculate current perceived brightness (luminance)
        float brightness = 0.2126f * r + 0.7152f * g + 0.0722f * b;

        // Avoid division by zero
        if (brightness == 0f) {
            // If color is completely black, just set all to the target
            r = g = b = target;
            return this;
        }

        // Compute scaling factor
        float scale = target / brightness;

        // Apply scale to RGB, clamp to [0, 1]
        r = Math.min(r * scale, 1f);
        g = Math.min(g * scale, 1f);
        b = Math.min(b * scale, 1f);

        return this;
    }

    public float getBrightness() {
        return 0.2126f * r + 0.7152f * g + 0.0722f * b;
    }

    public void fromARGB(int argb) {
        r = (argb >> 16 & 0xFF) / 255.0f;
        g = (argb >> 8 & 0xFF) / 255.0f;
        b = (argb & 0xFF) / 255.0f;
        a = (argb >> 24 & 0xFF) / 255.0f;
    }

    public Color copy() {
        return new Color(r, g, b, a);
    }
}