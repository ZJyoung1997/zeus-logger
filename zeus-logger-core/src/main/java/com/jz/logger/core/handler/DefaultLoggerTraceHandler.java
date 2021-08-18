package com.jz.logger.core.handler;

import com.jz.logger.core.LoggerInfo;
import com.jz.logger.core.LoggerResult;
import com.jz.logger.core.annotation.Logger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultLoggerTraceHandler implements LoggerTraceHandler {

    @Override
    public void execute(LoggerInfo loggerInfo) {
        Logger logger = loggerInfo.getLogger();
        for (LoggerResult loggerResult : loggerInfo.getLoggerResults()) {
            log.info("日志主题：{}，执行结果：{}", logger.topic(), loggerResult);
        }
    }

}
