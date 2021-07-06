package com.jz.logger.core.handler;

import com.jz.logger.core.annotation.Logger;

/**
 * @Author JZ
 * @Date 2021/7/6 15:17
 */
public interface LoggerHandler {

    void handleLogger(Object oldObject, Object newObject, Logger logger);

}
