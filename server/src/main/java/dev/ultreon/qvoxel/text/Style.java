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

package dev.ultreon.qvoxel.text;

public class Style {
    public static final Style EMPTY = new Style();
    private final int color;
    private final boolean bold;
    private final boolean italic;
    private final boolean underlined;
    private final boolean strikethrough;

    Style() {
        this(0xFFFFFFFF, false, false, false, false);
    }

    Style(int color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
    }

    public Style withColor(int color) {
        return new Style(color, bold, italic, underlined, strikethrough);
    }

    public Style withBold(boolean bold) {
        return new Style(color, bold, italic, underlined, strikethrough);
    }

    public Style withItalic(boolean italic) {
        return new Style(color, bold, italic, underlined, strikethrough);
    }

    public Style withUnderlined(boolean underlined) {
        return new Style(color, bold, italic, underlined, strikethrough);
    }

    public Style withStrikethrough(boolean strikethrough) {
        return new Style(color, bold, italic, underlined, strikethrough);
    }

    public int getColor() {
        return color;
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isUnderlined() {
        return underlined;
    }

    public boolean isStrikethrough() {
        return strikethrough;
    }
}
