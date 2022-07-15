package org.devopology.logger;

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
     * Method to a Logger
     *
     * @param prefix
     * @return Logger
     */
    public static Logger getLogger(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix is null");
        }

        return LOGGER_FACTORY.getOrCreateLogger(prefix);
    }
}
