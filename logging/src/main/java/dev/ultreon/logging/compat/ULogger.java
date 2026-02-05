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

package dev.ultreon.logging.compat;

import dev.ultreon.logging.LogCategory;
import dev.ultreon.logging.LogLevel;
import dev.ultreon.logging.Logger;
import dev.ultreon.logging.LoggerManager;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

class ULogger extends AbstractLogger {
    private static final Logger FAIL_LOGGER = LoggerManager.getLogger("Slf4J:Compat");
    private final Logger logger;

    ULogger(String name) {
        logger = LoggerManager.getLogger(name);
        this.name = name;
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isEnabled(LogLevel.TRACE);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.TRACE);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isEnabled(LogLevel.DEBUG);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isEnabled(LogLevel.INFO);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabled(LogLevel.WARN);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabled(LogLevel.ERROR);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.ERROR);
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        FormattingTuple format = format(messagePattern, arguments);
        if (format == null) return;
        if (throwable != null) {
            if (format.getThrowable() != null) {
                throwable.addSuppressed(format.getThrowable());
            }
        } else if (format.getThrowable() != null) {
            throwable = format.getThrowable();
        }
        logger.log(System.currentTimeMillis(), level(level), LogCategory.DEFAULT, format.getMessage(), throwable, false, false);
    }

    private FormattingTuple format(String messagePattern, Object[] arguments) {
        try {
            return MessageFormatter.arrayFormat(messagePattern, arguments);
        } catch (Exception e) {
            FAIL_LOGGER.error(LogCategory.DEFAULT, "Failed to format message! " + e.getMessage());
            return null;
        }
    }

    private LogLevel level(Level level) {
        return switch (level) {
            case TRACE -> LogLevel.TRACE;
            case DEBUG -> LogLevel.DEBUG;
            case WARN -> LogLevel.WARN;
            case ERROR -> LogLevel.ERROR;
            default -> LogLevel.INFO;
        };
    }
}
