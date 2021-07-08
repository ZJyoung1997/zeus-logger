package com.jz.logger.demo;

import com.jz.logger.core.LoggerInfo;
import com.jz.logger.core.handler.LoggerTraceHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author JZ
 * @Date 2021/7/8 14:32
 */
@Slf4j
public class CustomTraceHandler implements LoggerTraceHandler {

    @Override
    public void execute(LoggerInfo loggerInfo) {
        log.info("自定义LoggerTraceHandler");
    }

}
