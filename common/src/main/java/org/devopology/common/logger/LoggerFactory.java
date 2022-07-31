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

import org.devopology.common.precondition.Precondition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class to implement a LoggerFactory
 */
public class LoggerFactory {

    private static final LoggerFactory SINGLETON = new LoggerFactory();

    private static final Object LOCK = new Object();
    private Level defaultLevel = Level.INFO;
    private Map<String, Logger> loggerMap;

    /**
     * Constructor
     */
    private LoggerFactory() {
        loggerMap = new HashMap<>();
    }

    /**
     * Method get or a create a Logger
     *
     * @param name
     * @return Logger
     */
    private Logger getOrCreateLogger(String name) {
        Logger logger = null;

        synchronized (LOCK) {
            logger = loggerMap.get(name);
        }

        if (logger == null) {
            logger = new Logger(name);

            synchronized (LOCK) {
                logger.setLevel(defaultLevel);
                loggerMap.put(name, logger);
            }
        }

        return logger;
    }

    private void adjustLevel(Level level) {
        synchronized (LOCK) {
            defaultLevel = level;
        }

        synchronized (LOCK) {
            for (Map.Entry<String, Logger> entry : loggerMap.entrySet()) {
                entry.getValue().setLevel(level);
            }
        }
    }

    private void adjustLevel(String name, Level level) {
        Logger logger = null;

        synchronized (LOCK) {
            logger = loggerMap.get(name);
        }

        if (logger != null) {
            logger.setLevel(level);
        }
    }

    /**
     * Method to get a Logger
     */
    public static Logger getLogger(Class clazz) {
        Precondition.notNull(clazz, "class is null");

        return getLogger(clazz.getName());
    }

    /**
     * Method to get a Logger
     *
     * @param name
     * @return Logger
     */
    public static Logger getLogger(String name) {
        Precondition.notNull(name, "prefix is null");
        Precondition.notEmpty(name, "prefix is empty");

        return SINGLETON.getOrCreateLogger(name);
    }

    /**
     * Method to set the log level globally
     *
     * @param level
     */
    public static void setLevel(Level level) {
        SINGLETON.adjustLevel(level);
    }

    /**
     * Method to set the log level for a named logger
     *
     * @param name
     * @param level
     */
    public static void setLevel(String name, Level level) {
        Precondition.notNull(name, "name is null");
        Precondition.notEmpty(name, "name is empty");

        name = name.trim();

        SINGLETON.adjustLevel(name, level);
    }

    /**
     * Method to set the log level for a Class
     *
     * @param clazz
     * @param level
     */
    public static void setLevel(Class clazz, Level level) {
        Precondition.notNull(clazz, "class is null");

        String name = clazz.getName();
        SINGLETON.adjustLevel(name, level);
    }
}
