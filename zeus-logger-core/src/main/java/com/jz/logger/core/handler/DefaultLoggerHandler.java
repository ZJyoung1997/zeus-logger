package com.jz.logger.core.handler;

import cn.hutool.core.util.ArrayUtil;
import com.jz.logger.core.LoggerExtensionData;
import com.jz.logger.core.LoggerInfo;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.constant.Constants;
import com.jz.logger.core.enumerate.Strategy;
import com.jz.logger.core.event.LoggerEventProvider;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author JZ
 * @Date 2021/7/6 15:03
 */
public class DefaultLoggerHandler implements BeanFactoryAware, LoggerHandler {

    private final LoggerEventProvider loggerEventProvider;

    private List<LoggerExtensionData> globalExtDatas;

    private final int defaultRetryTimes;

    @Setter
    private List<Class<?>> globalExtDataClass;

    private BeanFactory beanFactory;

    private Strategy strategy;

    public DefaultLoggerHandler(LoggerEventProvider loggerEventProvider, Strategy strategy, int defaultRetryTimes) {
        this.defaultRetryTimes = defaultRetryTimes;
        this.loggerEventProvider = loggerEventProvider;
        if (strategy == null) {
            this.strategy = Strategy.ASYN_SERIAL;
        } else {
            this.strategy = strategy;
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        if (globalExtDataClass != null) {
            globalExtDatas = globalExtDataClass.stream()
                    .map(extDataClass -> (LoggerExtensionData) beanFactory.getBean(extDataClass))
                    .collect(Collectors.toList());
        } else {
            globalExtDataClass = null;
        }
    }

    @Override
    public void handleLogger(Object oldObject, Object newObject, Logger logger) {
        LoggerTraceHandler loggerTraceHandler;
        if (DefaultLoggerTraceHandler.class == logger.traceHandler()) {
            loggerTraceHandler = beanFactory.getBean(Constants.DEFAULT_LOGGER_TRACE_HANDLER, LoggerTraceHandler.class);
        } else {
            loggerTraceHandler = beanFactory.getBean(logger.traceHandler());
        }
        Strategy realStrategy = Strategy.DEFAULT == logger.strategy() ? this.strategy : logger.strategy();
        LoggerInfo loggerInfo = new LoggerInfo(oldObject, newObject, getExtData(oldObject, newObject, logger), logger);
        LoggerTraceHandler retryLoggerTraceHandler = new RetryLoggerTraceHandler(defaultRetryTimes, loggerTraceHandler);
        if (Strategy.SYNC == realStrategy) {
            retryLoggerTraceHandler.execute(loggerInfo);
        } else if (Strategy.ASYN_SERIAL == realStrategy) {
            loggerEventProvider.publishWithSerial(loggerInfo, retryLoggerTraceHandler);
        } else if (Strategy.ASYN_CONCURRENT == realStrategy) {
            loggerEventProvider.publishWithConcurrent(loggerInfo, retryLoggerTraceHandler);
        }
    }

    /**
     * 获取扩展数据
     */
    private Map<String, Object> getExtData(Object oldObject, Object newObject, Logger logger) {
        List<LoggerExtensionData> extensionDataList = new ArrayList<>();
        if (!logger.disableGlobalExtData() && globalExtDatas != null) {
            extensionDataList.addAll(globalExtDatas);
        }
        Class<?>[] customExtDatas = logger.customExtData();
        if (ArrayUtil.isNotEmpty(customExtDatas)) {
            extensionDataList.addAll(Arrays.stream(customExtDatas)
                    .map(extClazz -> (LoggerExtensionData) beanFactory.getBean(extClazz))
                    .collect(Collectors.toList()));
        }
        Map<String, Object> extDataMap = new HashMap<>();
        for (LoggerExtensionData loggerExtData : extensionDataList) {
            Map<String, Object> extData = loggerExtData.getExtData();
            if (extData != null) {
                extDataMap.putAll(extData);
            }
            extData = loggerExtData.getExtData(oldObject, newObject);
            if (extData != null) {
                extDataMap.putAll(extData);
            }
        }
        return extDataMap;
    }

}
