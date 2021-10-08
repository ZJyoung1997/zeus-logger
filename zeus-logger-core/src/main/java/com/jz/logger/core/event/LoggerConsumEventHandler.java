package com.jz.logger.core.event;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerConsumEventHandler implements EventHandler<LoggerConsumEvent>, WorkHandler<LoggerConsumEvent> {

    @Override
    public void onEvent(LoggerConsumEvent loggerConsumEvent, long sequence, boolean endOfBatch) {
        onEvent(loggerConsumEvent);
    }

    @Override
    public void onEvent(LoggerConsumEvent loggerConsumEvent) {
        try {
            loggerConsumEvent.getLoggerTraceHandler()
                    .execute(loggerConsumEvent.getLoggerInfo());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loggerConsumEvent.clear();
        }
    }

}
