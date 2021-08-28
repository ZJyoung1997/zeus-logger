package com.jz.logger.autoconfig;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.jz.logger.autoconfig.LoggerProperties.BaseDisruptorConfig;
import com.jz.logger.autoconfig.LoggerProperties.ConcurrentConfig;
import com.jz.logger.autoconfig.LoggerProperties.SerialConfig;
import com.jz.logger.core.LoggerExtensionData;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.aop.LoggerAroundAdvice;
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
import com.jz.logger.core.util.SpelUtils;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableConfigurationProperties(LoggerProperties.class)
public class LoggerConfiguration {

    @Resource
    private LoggerProperties loggerProperties;

    @Bean
    public SpelUtils spelUtils() {
        return new SpelUtils();
    }

    @Bean
    @ConditionalOnMissingBean(name = Constants.DEFAULT_LOGGER_TRACE_HANDLER)
    public LoggerTraceHandler defaultLoggerTraceHandler() {
        return new DefaultLoggerTraceHandler();
    }

    @Bean
    public LoggerHandler loggerHandler() {
        DefaultLoggerHandler loggerHandler = new DefaultLoggerHandler(loggerEventProvider(),
                loggerProperties.getDefaultStrategy(), loggerProperties.getDefaultRetryTimes());
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
    public LoggerAroundAdvice loggerAroundAdvice() {
        return new LoggerAroundAdvice(loggerHandler());
    }

    @Bean
    public Advisor zeusLoggerAdvisor() {
        Pointcut pointcut = new AnnotationMatchingPointcut(null, Logger.class, true);
        return new DefaultPointcutAdvisor(pointcut, loggerAroundAdvice());
    }

    @Bean
    public LoggerEventProvider loggerEventProvider() {
        return new LoggerEventProvider(serialDisruptor(), concurrentDisruptor());
    }

    private Disruptor<LoggerConsumEvent> concurrentDisruptor() {
        ConcurrentConfig concurrentConfig = loggerProperties.getDisruptor().getConcurrent();
        if (!concurrentConfig.isEnabled()) {
            return null;
        }
        EventFactory<LoggerConsumEvent> eventFactory = new LoggerEeventFactory();
        Disruptor<LoggerConsumEvent> disruptor = new Disruptor<>(
                eventFactory,
                concurrentConfig.getRingBufferSize(),
                ThreadFactoryBuilder.create().setNamePrefix(concurrentConfig.getThreadNamePrefix()).build(),
                ProducerType.SINGLE, getWaitStrategy(concurrentConfig)
        );
        int concurrentCustomerNum = concurrentConfig.getConcurrentCustomerNum();
        LoggerConsumEventHandler[] eventHandlers = new LoggerConsumEventHandler[concurrentCustomerNum];
        for (int i = 0; i < concurrentCustomerNum; i++) {
            eventHandlers[i] = new LoggerConsumEventHandler();
        }
        disruptor.handleEventsWithWorkerPool(eventHandlers);
        return disruptor;
    }

    private Disruptor<LoggerConsumEvent> serialDisruptor() {
        SerialConfig serialConfig = loggerProperties.getDisruptor().getSerial();
        if (!serialConfig.isEnabled()) {
            return null;
        }
        EventFactory<LoggerConsumEvent> eventFactory = new LoggerEeventFactory();
        Disruptor<LoggerConsumEvent> disruptor = new Disruptor<>(
                eventFactory,
                serialConfig.getRingBufferSize(),
                ThreadFactoryBuilder.create().setNamePrefix(serialConfig.getThreadNamePrefix()).build(),
                ProducerType.SINGLE, getWaitStrategy(serialConfig)
        );
        LoggerConsumEventHandler eventHandler = new LoggerConsumEventHandler();
        disruptor.handleEventsWith(eventHandler);
        return disruptor;
    }

    private WaitStrategy getWaitStrategy(BaseDisruptorConfig config) {
        switch (config.getWaitStrategy()) {
            case BLOCKING: return new BlockingWaitStrategy();
            case YIELDING: return new YieldingWaitStrategy();
            case SLEEPING:
            default: return new SleepingWaitStrategy(config.getSleepingWaitStrategyRetries(), config.getSleepingWaitStrategySleepTime());
        }
    }

}
