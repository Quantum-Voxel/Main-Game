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

import dev.ultreon.libs.translations.v1.Language;

public class TranslationText extends SimpleMutableText {
    private final String path;
    private final Object[] args;

    public TranslationText(String path, Object... args) {
        this.path = path;
        this.args = args;
    }

    @Override
    protected String getStringImpl() {
        String[] args = new String[this.args.length];
        for (int i = 0; i < this.args.length; i++)
            args[i] = this.args[i] instanceof Text text ? text.getText() : this.args[i].toString();

        return Language.translate(path, (Object[]) args);
    }

    @Override
    public MutableText copy() {
        return new TranslationText(path, args);
    }
}
