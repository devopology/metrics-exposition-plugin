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

package org.devopology.java.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to implement a LoggerFactory
 */
public class LoggerFactory {

    private final static LoggerFactory LOGGER_FACTORY = new LoggerFactory();

    private Map<String, Logger> loggerMap;

    /**
     * Constructor
     */
    private LoggerFactory() {
        this.loggerMap = new HashMap<String, Logger>();
    }

    /**
     * Method get or a create a Logger
     *
     * @param prefix
     * @return Logger
     */
    private synchronized Logger getOrCreateLogger(String prefix) {
        Logger logger = this.loggerMap.get(prefix);
        if (logger == null) {
            logger = new Logger(prefix);
            this.loggerMap.put(prefix, logger);
        }
        return logger;
    }

    /**
     * Method to get a Logger
     */
    public static Logger getLogger(Class clazz) {
        notNull(clazz, "class is null");
        return getLogger(clazz.getName());
    }

    /**
     * Method to get a Logger
     *
     * @param prefix
     * @return Logger
     */
    public static Logger getLogger(String prefix) {
        notNull(prefix, "prefix is null");
        notEmpty(prefix, "prefix is empty");
        return LOGGER_FACTORY.getOrCreateLogger(prefix);
    }

    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void notEmpty(String string, String message) {
        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
