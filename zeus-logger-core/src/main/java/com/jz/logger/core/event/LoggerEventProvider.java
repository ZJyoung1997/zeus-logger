package com.jz.logger.core.event;

import com.jz.logger.core.LoggerInfo;
import com.jz.logger.core.handler.LoggerTraceHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import javax.annotation.PostConstruct;

public class LoggerEventProvider {

    private final Disruptor<LoggerConsumEvent> serialDisruptor;

    private final Disruptor<LoggerConsumEvent> concurrentDisruptor;

    private final RingBuffer<LoggerConsumEvent> serialRingBuffer;

    private final RingBuffer<LoggerConsumEvent> concurrentRingBuffer;

    public LoggerEventProvider(Disruptor<LoggerConsumEvent> serialDisruptor, Disruptor<LoggerConsumEvent> concurrentDisruptor) {
        this.serialDisruptor = serialDisruptor;
        this.concurrentDisruptor = concurrentDisruptor;
        this.serialRingBuffer = serialDisruptor.getRingBuffer();
        this.concurrentRingBuffer = concurrentDisruptor.getRingBuffer();
    }

    @PostConstruct
    private void init() {
        serialDisruptor.start();
        concurrentDisruptor.start();
    }

    public void publishWithSerial(LoggerInfo loggerInfo, LoggerTraceHandler loggerTraceHandler) {
        long sequence = serialRingBuffer.next();
        try {
            LoggerConsumEvent loggerConsumEvent = serialRingBuffer.get(sequence);
            loggerConsumEvent.setLoggerInfo(loggerInfo);
            loggerConsumEvent.setLoggerTraceHandler(loggerTraceHandler);
        } finally {
            serialRingBuffer.publish(sequence);
        }
    }

    public void publishWithConcurrent(LoggerInfo loggerInfo, LoggerTraceHandler loggerTraceHandler) {
        long sequence = concurrentRingBuffer.next();
        try {
            LoggerConsumEvent loggerConsumEvent = concurrentRingBuffer.get(sequence);
            loggerConsumEvent.setLoggerInfo(loggerInfo);
            loggerConsumEvent.setLoggerTraceHandler(loggerTraceHandler);
        } finally {
            concurrentRingBuffer.publish(sequence);
        }
    }

}
