package com.jz.logger.core.handler;

import com.jz.logger.core.LoggerInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultLoggerTraceHandler implements LoggerTraceHandler {

    @Override
    public void execute(LoggerInfo loggerInfo) {
        log.info("操作类型：{}，操作执行前：{}", loggerInfo.getTopic(), loggerInfo.getTraceInfos());
        log.info("操作类型：{}，操作执行后：{}", loggerInfo.getTopic(), loggerInfo.getTraceInfos());
    }

}
