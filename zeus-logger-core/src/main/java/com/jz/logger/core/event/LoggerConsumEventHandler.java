package com.jz.logger.core.event;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

public class LoggerConsumEventHandler implements EventHandler<LoggerConsumEvent>, WorkHandler<LoggerConsumEvent> {

    @Override
    public void onEvent(LoggerConsumEvent loggerConsumEvent, long sequence, boolean endOfBatch) throws Exception {
        onEvent(loggerConsumEvent);
    }

    @Override
    public void onEvent(LoggerConsumEvent loggerConsumEvent) throws Exception {
        loggerConsumEvent.getLoggerTraceHandler()
                .execute(loggerConsumEvent.getLoggerInfo());
    }

}
