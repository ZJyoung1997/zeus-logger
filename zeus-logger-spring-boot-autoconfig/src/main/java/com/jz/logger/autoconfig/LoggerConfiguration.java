package com.jz.logger.autoconfig;

import cn.hutool.core.collection.CollUtil;
import com.jz.logger.core.LoggerExtensionData;
import com.jz.logger.core.aspect.LoggerAspect;
import com.jz.logger.core.constant.Constants;
import com.jz.logger.core.event.LoggerConsumEvent;
import com.jz.logger.core.event.LoggerConsumEventHandler;
import com.jz.logger.core.event.LoggerEeventFactory;
import com.jz.logger.core.event.LoggerEventProvider;
import com.jz.logger.core.handler.DefaultLoggerHandler;
import com.jz.logger.core.handler.DefaultLoggerTraceHandler;
import com.jz.logger.core.handler.LoggerHandler;
import com.jz.logger.core.handler.LoggerTraceHandler;
import com.jz.logger.core.util.ClassUtils;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableConfigurationProperties(LoggerProperties.class)
public class LoggerConfiguration {

    @Resource
    private LoggerProperties loggerProperties;

    @Bean
    @ConditionalOnMissingBean(name = Constants.DEFAULT_LOGGER_TRACE_HANDLER)
    public LoggerTraceHandler defaultLoggerTraceHandler() {
        return new DefaultLoggerTraceHandler();
    }

    @Bean
    public LoggerHandler loggerHandler() {
        DefaultLoggerHandler loggerHandler = new DefaultLoggerHandler(loggerEventProvider(), loggerProperties.getDefaultStrategy());
        List<String> globalExtDataClass = loggerProperties.getGlobalExtensionDatas();
        if (CollUtil.isNotEmpty(globalExtDataClass)) {
            List<Class<?>> extensionDatas = globalExtDataClass.stream().distinct()
                    .map(extDataClass -> {
                        try {
                            Class<?> clazz = Class.forName(extDataClass);
                            if (ClassUtils.hasInterface(clazz, LoggerExtensionData.class)) {
                                log.info("load global LoggerExtensionData: {}", extDataClass);
                                return clazz;
                            } else {
                                log.warn("skip {} . Because unimplemented com.jz.logger.core.LoggerExtensionData", extDataClass);
                            }
                        } catch (ClassNotFoundException e) {
                            log.warn(e.getMessage());
                        }
                        return null;
                    }).filter(Objects::nonNull).collect(Collectors.toList());
            loggerHandler.setGlobalExtDataClass(extensionDatas);
        }
        return loggerHandler;
    }

    @Bean
    public LoggerAspect loggerAspect() {
        return new LoggerAspect(loggerHandler());
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
