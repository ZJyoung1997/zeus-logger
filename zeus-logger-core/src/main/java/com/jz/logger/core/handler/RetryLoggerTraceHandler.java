package com.jz.logger.core.handler;

import com.jz.logger.core.LoggerInfo;
import com.jz.logger.core.annotation.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * @Author JZ
 * @Date 2021/8/17 17:17
 */
@Slf4j
public class RetryLoggerTraceHandler implements LoggerTraceHandler {

    private final int defaultRetryTimes;

    private final LoggerTraceHandler loggerTraceHandler;

    public RetryLoggerTraceHandler(int defaultRetryTimes, LoggerTraceHandler loggerTraceHandler) {
        Assert.notNull(loggerTraceHandler, "LoggerTraceHandler cannot be null");
        this.defaultRetryTimes = defaultRetryTimes;
        this.loggerTraceHandler = loggerTraceHandler;
    }

    @Override
    public void execute(LoggerInfo loggerInfo) {
        execute(loggerInfo, 0);
    }

    private void execute(LoggerInfo loggerInfo, int times) {
        try {
            loggerTraceHandler.execute(loggerInfo);
        } catch (Throwable e) {
            if (isTermination(times, loggerInfo.getLogger(), e)) {
                log.warn("日志记录失败：{}\n异常信息：", loggerInfo, e);
            } else {
                execute(loggerInfo, times + 1);
            }
        }
    }

    private boolean isTermination(int times, Logger logger, Throwable throwable) {
        int retryTimes = logger.retryTimes() == -1 ? defaultRetryTimes : logger.retryTimes();
        if (retryTimes <= 0) {
            return true;
        }
        for (Class<?> clazz : logger.noRetryFor()) {
            if (clazz == throwable.getClass()) {
                return true;
            }
        }
        boolean includeExection = false;
        for (Class<?> clazz : logger.retryFor()) {
            if (clazz == throwable.getClass()) {
                includeExection = true;
            }
        }
        if (logger.retryFor().length == 0) {
            includeExection = true;
        }
        return !includeExection || times >= retryTimes;
    }

}
