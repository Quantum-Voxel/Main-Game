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

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public abstract class SimpleMutableText implements MutableText {
    private final List<Text> extra = new ArrayList<>();
    private Style style = new Style();

    protected abstract String getStringImpl();

    public abstract MutableText copy();

    @Override
    public String getText() {
        StringBuilder builder = new StringBuilder(getStringImpl());
        for (Text text : extra) {
            builder.append(text.getText());
        }
        return builder.toString();
    }

    public Collection<Text> getExtra() {
        return Collections.unmodifiableCollection(extra);
    }

    @Override
    public void append(Text text) {
        extra.add(text);
    }

    public Style withStyle(Function<Style, Style> styler) {
        return style = styler.apply(style);
    }

    public Style getStyle() {
        return style;
    }

    void setStyle(Style style) {
        this.style = style;
    }

    @Override
    public @NotNull Iterator<Text> iterator() {
        return new TextIterator();
    }

    private class TextIterator implements Iterator<Text> {
        private int index = 0;
        private Iterator<Text> extraIterator = extra.iterator();

        @Override
        public boolean hasNext() {
            return index < extra.size() + 1 || extraIterator != null && extraIterator.hasNext();
        }

        @Override
        public Text next() {
            if (extraIterator.hasNext()) return extraIterator.next();
            else extraIterator = null;

            int i = index++;
            if (i == 0) return new Text() {
                @Override
                public Style getStyle() {
                    return SimpleMutableText.this.getStyle();
                }

                @Override
                public String getText() {
                    return getStringImpl();
                }
            };
            Text text = extra.get(i - 1);
            if (text instanceof MutableText mutableText)
                extraIterator = mutableText.iterator();
            return text;
        }
    }
}
