package com.jz.logger.core.handler;

import cn.hutool.core.util.ArrayUtil;
import com.jz.logger.core.LoggerExtensionData;
import com.jz.logger.core.LoggerInfo;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.enumerate.Strategy;
import com.jz.logger.core.event.LoggerEventProvider;
import com.jz.logger.core.util.ClassUtils;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author JZ
 * @Date 2021/7/6 15:03
 */
public class DefaultLoggerHandler implements BeanFactoryAware, LoggerHandler {

    private static final String DEFAULT_LOGGER_TRACE_HANDLER_BEAN_NAME = "defaultLoggerTraceHandler";

    private final Map<String, LoggerTraceHandler> loggerTraceHandlerCache = new ConcurrentReferenceHashMap<>();

    private final LoggerEventProvider loggerEventProvider;

    private List<LoggerExtensionData> globalExtDatas;

    @Setter
    private List<Class<?>> globalExtDataClass;

    private BeanFactory beanFactory;


    public DefaultLoggerHandler(LoggerEventProvider loggerEventProvider) {
        this.loggerEventProvider = loggerEventProvider;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        loggerTraceHandlerCache.put(DEFAULT_LOGGER_TRACE_HANDLER_BEAN_NAME, beanFactory.getBean(DEFAULT_LOGGER_TRACE_HANDLER_BEAN_NAME, LoggerTraceHandler.class));
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
        Strategy strategy = logger.strategy();
        LoggerInfo loggerInfo = new LoggerInfo(oldObject, newObject, getExtData(oldObject, logger), logger);
        if (Strategy.SYNC == strategy) {
            loggerTraceHandler.execute(loggerInfo);
        } else if (Strategy.ASYN_SERIAL == strategy) {
            loggerEventProvider.publishWithSerial(loggerInfo, loggerTraceHandler);
        } else if (Strategy.ASYN_CONCURRENT == strategy) {
            loggerEventProvider.publishWithConcurrent(loggerInfo, loggerTraceHandler);
        }
    }

    /**
     * 获取扩展数据
     */
    private Map<String, Object> getExtData(Object oldObject, Logger logger) {
        List<LoggerExtensionData> extensionDataList = new ArrayList<>();
        if (!logger.disableGlobalExtData() && globalExtDatas != null) {
            extensionDataList.addAll(globalExtDatas);
        }
        Class<?>[] customExtDatas = logger.customExtData();
        if (ArrayUtil.isNotEmpty(customExtDatas)) {
            extensionDataList.addAll(Arrays.stream(customExtDatas)
                    .filter(extClazz -> ClassUtils.hasInterface(extClazz, LoggerExtensionData.class))
                    .map(extClazz -> (LoggerExtensionData) beanFactory.getBean(extClazz))
                    .collect(Collectors.toList()));
        }
        Map<String, Object> extDataMap = new HashMap<>(extensionDataList.size());
        for (LoggerExtensionData globalExtData : extensionDataList) {
            Map<String, Object> extData = globalExtData.getExtData();
            if (extData != null) {
                extDataMap.putAll(extData);
            }
            extData = globalExtData.getExtData(oldObject);
            if (extData != null) {
                extDataMap.putAll(extData);
            }
        }
        return extDataMap;
    }

    private LoggerTraceHandler getLoggerTraceHandler(String loggerHandlerBeanName) {
        LoggerTraceHandler loggerTraceHandler = loggerTraceHandlerCache.get(loggerHandlerBeanName);
        if (loggerTraceHandler == null) {
            loggerTraceHandler = beanFactory.getBean(loggerHandlerBeanName, LoggerTraceHandler.class);
            loggerTraceHandlerCache.put(loggerHandlerBeanName, loggerTraceHandler);
        }
        return loggerTraceHandler;
    }

}
