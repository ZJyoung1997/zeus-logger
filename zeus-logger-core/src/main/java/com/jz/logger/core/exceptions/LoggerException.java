package com.jz.logger.core.exceptions;

/**
 * @Author JZ
 * @Date 2021/8/27 15:04
 */
public class LoggerException extends RuntimeException {

    public LoggerException(String message) {
        super(message);
    }

    public LoggerException(Throwable cause) {
        super(cause);
    }

    public LoggerException(String message, Throwable cause) {
        super(message, cause);
    }

}
