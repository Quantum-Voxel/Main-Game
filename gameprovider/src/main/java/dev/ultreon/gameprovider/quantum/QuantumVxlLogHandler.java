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

package dev.ultreon.gameprovider.quantum;

import dev.ultreon.logging.Logger;
import dev.ultreon.logging.LoggerManager;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogHandler;
import net.fabricmc.loader.impl.util.log.LogLevel;

/**
 * QuantumVxlLogHandler class that implements LogHandler interface.
 * Responsible for handling logging operations.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class QuantumVxlLogHandler implements LogHandler {
    private final Logger logger = LoggerManager.getLogger("Quantum Voxel");

    /**
     * Log a message with the specified level, category, message, exception, and other flags.
     *
     * @param time          The time of the log event
     * @param level         The log level
     * @param category      The category of the log
     * @param msg           The message to log
     * @param exc           The exception to log
     * @param fromReplay    Flag indicating if the log is from a replay
     * @param wasSuppressed Flag indicating if the log was suppressed
     */
    @Override
    public void log(long time, LogLevel level, LogCategory category, String msg, Throwable exc, boolean fromReplay, boolean wasSuppressed) {
        logger.log(time, levelFor(level), categoryFor(category), msg, exc, fromReplay, wasSuppressed);
    }

    private dev.ultreon.logging.LogCategory categoryFor(LogCategory category) {
        if (category == null) return dev.ultreon.logging.LogCategory.DEFAULT;
        return new dev.ultreon.logging.LogCategory(category.name);
    }

    private dev.ultreon.logging.LogLevel levelFor(LogLevel level) {
        return switch (level) {
            case TRACE -> dev.ultreon.logging.LogLevel.TRACE;
            case DEBUG -> dev.ultreon.logging.LogLevel.DEBUG;
            case WARN -> dev.ultreon.logging.LogLevel.WARN;
            case ERROR -> dev.ultreon.logging.LogLevel.ERROR;
            default -> dev.ultreon.logging.LogLevel.INFO;
        };
    }

    /**
     * Check if logging is enabled for the specified level and category.
     *
     * @param level    The log level
     * @param category The log category
     * @return True if logging is enabled, false otherwise
     */
    @Override
    public boolean shouldLog(LogLevel level, LogCategory category) {
        return true;
    }

    /**
     * Close method for any necessary cleanup operations.
     */
    @Override
    public void close() {
        // No cleanup needed at the moment
    }
}
