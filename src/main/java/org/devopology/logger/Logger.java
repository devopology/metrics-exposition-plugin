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

package org.devopology.logger;

/**
 * Class to implement a Logger
 */
public class Logger {

    private String prefix;

    /**
     * Constructor
     *
     * @param prefix
     */
    Logger(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Method to log an info message
     *
     * @param message
     */
    public void info(String message) {
        log("INFO", message);
    }

    /**
     * Method to log a warning message
     *
     * @param message
     */
    public void warn(String message) {
        log("WARN", message);
    }

    /**
     * Method to log an error message
     * @param message
     */
    public void error(String message) {
        log("ERROR", message);
    }

    /**
     * Method to log a message
     *
     * @param level
     * @param message
     */
    private void log(String level, String message) {
        System.out.println(this.prefix + " " + level + " " + message);
    }
}
