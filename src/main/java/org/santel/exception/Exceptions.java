package org.santel.exception;

import org.slf4j.*;

public class Exceptions {
    public static void logAndThrow(Logger logger, String errorMessage) {
        logger.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }
}
