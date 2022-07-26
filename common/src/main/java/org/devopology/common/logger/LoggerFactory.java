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

/**
 * Class to implement a LoggerFactory
 */
public class LoggerFactory {

    private static final LoggerFactory LOGGER_FACTORY = new LoggerFactory();

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

        return LOGGER_FACTORY.getOrCreateLogger(name);
    }
}
