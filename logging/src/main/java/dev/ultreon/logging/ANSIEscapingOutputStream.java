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

package dev.ultreon.logging;

import java.io.IOException;
import java.io.OutputStream;

class ANSIEscapingOutputStream extends OutputStream {
    private final OutputStream delegate;
    private boolean prefixing;
    private boolean escaping;

    public ANSIEscapingOutputStream(OutputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\u001B') {
            prefixing = true;
        } else if (b == '[' && prefixing) {
            prefixing = false;
            escaping = true;
        } else if (!escaping || prefixing) {
            if (prefixing) delegate.write('\u001B');
            delegate.write(b);
        } else if (b == 'm') {
            escaping = false;
        }
    }
}
