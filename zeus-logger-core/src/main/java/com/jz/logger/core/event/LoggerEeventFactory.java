package com.jz.logger.core.event;

import com.lmax.disruptor.EventFactory;

public class LoggerEeventFactory implements EventFactory<LoggerConsumEvent> {

    @Override
    public LoggerConsumEvent newInstance() {
        return new LoggerConsumEvent();
    }

}
