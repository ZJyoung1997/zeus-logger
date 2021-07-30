package com.jz.logger.core.handler;

import cn.hutool.core.util.ArrayUtil;
import com.jz.logger.core.LoggerExtensionData;
import com.jz.logger.core.LoggerInfo;
import com.jz.logger.core.annotation.Logger;
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

    @Setter
    private List<Class<?>> globalExtDataClass;

    private BeanFactory beanFactory;

    private Strategy strategy;

    public DefaultLoggerHandler(LoggerEventProvider loggerEventProvider, Strategy strategy) {
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
        LoggerTraceHandler loggerTraceHandler = beanFactory.getBean(logger.handlerBeanName(), LoggerTraceHandler.class);
        Strategy realStrategy = Strategy.DEFAULT == logger.strategy() ? this.strategy : logger.strategy();
        LoggerInfo loggerInfo = new LoggerInfo(oldObject, newObject, getExtData(oldObject, newObject, logger), logger);
        if (Strategy.SYNC == realStrategy) {
            loggerTraceHandler.execute(loggerInfo);
        } else if (Strategy.ASYN_SERIAL == realStrategy) {
            loggerEventProvider.publishWithSerial(loggerInfo, loggerTraceHandler);
        } else if (Strategy.ASYN_CONCURRENT == realStrategy) {
            loggerEventProvider.publishWithConcurrent(loggerInfo, loggerTraceHandler);
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
