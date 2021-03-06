/*
 * Copyright 2022 Douglas Hoard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devopology.common.logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to implement a Logger
 */
public class Logger {

    private static final String LOG_FORMAT = "[%s] %s %s - %s";

    private AtomicInteger level;
    private String name;

    /**
     * Constructor
     *
     * @param name
     */
    Logger(String name) {
        this.name = name;
        this.level = new AtomicInteger(decode(Level.INFO));
    }

    public void setLevel(Level level) {
        this.level.set(decode(level));
    }

    /**
     * Method to log an info message
     *
     * @param message
     */
    public void trace(String message) {
        log(Level.TRACE, message);
    }

    /**
     * Method to log an info message
     *
     * @param message
     */
    public void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Method to log a warning message
     *
     * @param message
     */
    public void warn(String message) {
        log(Level.WARN, message);
    }

    /**
     * Method to log an error message
     * @param message
     */
    public void error(String message) {
        log(Level.ERROR, message);
    }

    /**
     * Method to log a message
     *
     * @param level
     * @param message
     */
    public void log(Level level, String message) {
        int levelInt = decode(level);
        if (this.level.get() >= levelInt) {
            System.out.println(String.format(LOG_FORMAT, Thread.currentThread().getName(), level, name, message));
            System.out.flush();
        }
    }

    private int decode(Level level) {
        switch (level) {
            case ERROR: {
                return 0;
            }
            case WARN: {
                return 10;
            }
            case INFO: {
                return 20;
            }

            case TRACE: {
                return 30;
            }
        }

        return 20;
    }
}
