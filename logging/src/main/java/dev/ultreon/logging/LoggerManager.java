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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class LoggerManager {
    private static final LoggerManager INSTANCE = new LoggerManager();
    private final Map<String, Logger> loggers = new HashMap<>();
    public final PrintStream out;
    public final PrintStream debugOut;
    private final PrintStream oldOut;
    private final PrintStream oldErr;
    private LogLevel level = LogLevel.INFO;

    private LoggerManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        if (System.getProperty("ultreon.log.level") != null) {
            try {
                level = LogLevel.valueOf(System.getProperty("ultreon.log.level").toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid log level: " + System.getProperty("ultreon.log.level"));
            }
        }

        if (Files.notExists(new File("logs").toPath())) {
            try {
                Files.createDirectories(new File("logs").toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create log directory", e);
            }
        }

        compressIfExists(Paths.get("logs/latest.log"));
        compressIfExists(Paths.get("logs/debug.log"));

        try {
            debugOut = new MultiPrintStream(true, System.out, new PrintStream(new ANSIEscapingOutputStream(Files.newOutputStream(
                    new File("logs/debug.log").toPath()
            )), true));
            out = new MultiPrintStream(true, debugOut, new PrintStream(new ANSIEscapingOutputStream(Files.newOutputStream(
                    new File("logs/latest.log").toPath()
            )), true));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create log file", e);
        }

        oldOut = System.out;
        oldErr = System.err;
    }

    private void compressIfExists(Path path) {
        if (Files.exists(path)) {
            compress(path, Paths.get("logs/latest.log.gz"));
        }
    }

    private void compress(Path path, Path out) {
        try {
            Files.deleteIfExists(out);
            Files.createFile(out);

            try (GZIPOutputStream gzip = new GZIPOutputStream(Files.newOutputStream(out))) {
                byte[] buffer = new byte[1024];
                int len;
                try (InputStream in = Files.newInputStream(path)) {
                    while ((len = in.read(buffer)) > 0) {
                        gzip.write(buffer, 0, len);
                    }
                }

                gzip.finish();
            }
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress log file", e);
        }
    }

    private void close() {
        out.close();

        System.setOut(oldOut);
        System.setErr(oldErr);

        loggers.clear();
        INSTANCE.loggers.clear();
    }

    public static Logger getLogger(String name) {
        return INSTANCE.loggers.computeIfAbsent(name, INSTANCE::createLogger);
    }

    public Logger createLogger(String name) {
        return new Logger(this, name);
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }
}
