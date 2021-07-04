package com.jz.logger.autoconfig;

import com.jz.logger.core.aspect.LoggerAspect;
import com.jz.logger.core.event.LoggerConsumEvent;
import com.jz.logger.core.event.LoggerConsumEventHandler;
import com.jz.logger.core.event.LoggerEeventFactory;
import com.jz.logger.core.event.LoggerEventProvider;
import com.jz.logger.core.handler.DefaultLoggerTraceHandler;
import com.jz.logger.core.handler.LoggerTraceHandler;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.Executors;

@Configuration()
@EnableConfigurationProperties(LoggerProperties.class)
public class LoggerConfiguration {

    @Resource
    private LoggerProperties loggerProperties;

    @Bean
    public LoggerTraceHandler defaultLoggerTraceHandler() {
        return new DefaultLoggerTraceHandler();
    }

    @Bean
    public LoggerAspect loggerAspect() {
        return new LoggerAspect(loggerEventProvider());
    }

    @Bean
    public LoggerEventProvider loggerEventProvider() {
        return new LoggerEventProvider(serialDisruptor(), concurrentDisruptor());
    }

    private Disruptor<LoggerConsumEvent> concurrentDisruptor() {
        EventFactory<LoggerConsumEvent> eventFactory = new LoggerEeventFactory();
        Disruptor<LoggerConsumEvent> disruptor = new Disruptor<>(
                eventFactory,
                loggerProperties.getConcurrentRingBufferSize(),
                Executors.defaultThreadFactory(),
                ProducerType.SINGLE, new YieldingWaitStrategy()
        );
        Integer concurrentNum = loggerProperties.getConcurrentNum();
        if (concurrentNum == null) {
            concurrentNum = Runtime.getRuntime().availableProcessors();
        }
        LoggerConsumEventHandler[] eventHandlers = new LoggerConsumEventHandler[concurrentNum];
        for (int i = 0; i < concurrentNum; i++) {
            eventHandlers[i] = new LoggerConsumEventHandler();
        }
        disruptor.handleEventsWithWorkerPool(eventHandlers);
        return disruptor;
    }

    private Disruptor<LoggerConsumEvent> serialDisruptor() {
        EventFactory<LoggerConsumEvent> eventFactory = new LoggerEeventFactory();
        Disruptor<LoggerConsumEvent> disruptor = new Disruptor<>(
                eventFactory,
                loggerProperties.getSerialRingBufferSize(),
                Executors.defaultThreadFactory(),
                ProducerType.SINGLE, new YieldingWaitStrategy()
        );
        LoggerConsumEventHandler eventHandler = new LoggerConsumEventHandler();
        disruptor.handleEventsWith(eventHandler);
        return disruptor;
    }

}
