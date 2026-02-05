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
import java.io.PrintStream;

public class LogOutputStream extends OutputStream {
    private final Logger logger;
    private final StringBuffer buffer = new StringBuffer();
    private final PrintStream out;

    public LogOutputStream(LoggerManager loggerManager, PrintStream out, String name) {
        super();
        this.out = out;

        logger = loggerManager.createLogger(name);
    }

    @Override
    public void write(int b) throws IOException {
        synchronized (this) {
            if (b == '\n' || b == '\r') {
                flush();
            } else {
                buffer.append((char) b);
            }
        }
    }

    @Override
    public void flush() {
        if (buffer.length() == 0) {
            return;
        }
        try {
            logger.log(System.currentTimeMillis(), LogLevel.INFO, LogCategory.DEFAULT, buffer.toString(), null, false, false);
        } catch (Throwable e) {
            out.println("Failed to log message: " + e.getMessage());
        }
        buffer.setLength(0);
    }
}
