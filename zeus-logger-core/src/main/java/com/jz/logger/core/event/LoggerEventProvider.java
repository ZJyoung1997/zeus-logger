package com.jz.logger.core.event;

import com.jz.logger.core.LoggerInfo;
import com.jz.logger.core.enumerate.Strategy;
import com.jz.logger.core.handler.LoggerTraceHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

public class LoggerEventProvider {

    private final Disruptor<LoggerConsumEvent> serialDisruptor;

    private final Disruptor<LoggerConsumEvent> concurrentDisruptor;

    private final RingBuffer<LoggerConsumEvent> serialRingBuffer;

    private final RingBuffer<LoggerConsumEvent> concurrentRingBuffer;

    public LoggerEventProvider(Disruptor<LoggerConsumEvent> serialDisruptor, Disruptor<LoggerConsumEvent> concurrentDisruptor) {
        this.serialDisruptor = serialDisruptor;
        this.concurrentDisruptor = concurrentDisruptor;
        this.serialRingBuffer = serialDisruptor == null ? null : serialDisruptor.getRingBuffer();
        this.concurrentRingBuffer = concurrentDisruptor == null ? null : concurrentDisruptor.getRingBuffer();
    }

    @PostConstruct
    private void init() {
        if (serialDisruptor != null) {
            serialDisruptor.start();
            Runtime.getRuntime().addShutdownHook(new Thread(new DisruptorShutdownHook(serialDisruptor, "serialDisruptor")));
        }
        if (concurrentDisruptor != null) {
            concurrentDisruptor.start();
            Runtime.getRuntime().addShutdownHook(new Thread(new DisruptorShutdownHook(concurrentDisruptor, "concurrentDisruptor")));
        }
    }

    public void publishWithSerial(LoggerInfo loggerInfo, LoggerTraceHandler loggerTraceHandler) {
        if (serialRingBuffer == null) {
            throw new IllegalStateException(Strategy.ASYN_SERIAL + " strategy is not turned on");
        }
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
        if (concurrentRingBuffer == null) {
            throw new IllegalStateException(Strategy.ASYN_CONCURRENT + " strategy is not turned on");
        }
        long sequence = concurrentRingBuffer.next();
        try {
            LoggerConsumEvent loggerConsumEvent = concurrentRingBuffer.get(sequence);
            loggerConsumEvent.setLoggerInfo(loggerInfo);
            loggerConsumEvent.setLoggerTraceHandler(loggerTraceHandler);
        } finally {
            concurrentRingBuffer.publish(sequence);
        }
    }

    public boolean supportStrategy(Strategy strategy) {
        if (Strategy.ASYN_SERIAL == strategy) {
            return serialDisruptor != null;
        } else if (Strategy.ASYN_CONCURRENT == strategy) {
            return concurrentDisruptor != null;
        } else {
            return false;
        }
    }

    class DisruptorShutdownHook implements ShutdownHook, Runnable {

        final Logger logger = LoggerFactory.getLogger(DisruptorShutdownHook.class);

        private final Disruptor disruptor;

        private String name;

        private final long startTime;

        DisruptorShutdownHook(Disruptor disruptor, String name) {
            this.disruptor = disruptor;
            this.name = name;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            shutdown();
        }

        @Override
        public void shutdown() {
            logger.info("Start shutdown Disrupt '{}'", name);
            try {
                if (System.currentTimeMillis() - startTime < 1000) {
                    Thread.sleep(1000);
                }
                disruptor.shutdown();
            } catch (InterruptedException e) {
                disruptor.shutdown();
            }
            logger.info("Successfully shutdown Disrupt '{}'", name);
        }

    }

    interface ShutdownHook {

        void shutdown();

    }

}
