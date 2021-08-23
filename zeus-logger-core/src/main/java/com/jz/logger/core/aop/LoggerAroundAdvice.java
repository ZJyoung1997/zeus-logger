package com.jz.logger.core.aop;

import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.converters.Converter;
import com.jz.logger.core.converters.DefaultMethodParameterConverter;
import com.jz.logger.core.handler.LoggerHandler;
import com.jz.logger.core.holder.LoggerHolder;
import com.jz.logger.core.util.ClassUtils;
import com.jz.logger.core.util.MethodUtils;
import com.jz.logger.core.util.SpelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @Author JZ
 * @Date 2021/8/6 14:20
 */
@Slf4j
public class LoggerAroundAdvice implements MethodBeforeAdvice, AfterReturningAdvice {

    private final ThreadLocal<Object> oldObject = new ThreadLocal<>();

    private final ThreadLocal<Object> newObject = new ThreadLocal<>();

    private final ThreadLocal<Object> selectParam = new ThreadLocal<>();

    private final LoggerHandler loggerHandler;

    public LoggerAroundAdvice(LoggerHandler loggerHandler) {
        this.loggerHandler = loggerHandler;
    }

    @Override
    @SneakyThrows
    public void before(Method method, Object[] args, Object targetObject) {
        Logger logger = MethodUtils.getMethodAnnotation(method, Logger.class);

        newObject.remove();
        oldObject.remove();
        selectParam.remove();
        if (logger.enabledManual()) {
            return;
        }
        selectParam.set(getSelectParam(logger, args));
        oldObject.set(SpelUtils.getValue(logger.selectMethod(), selectParam.get()));
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object o1) {
        Logger logger = MethodUtils.getMethodAnnotation(method, Logger.class);
        if (logger.enabledManual()) {
            if (LoggerHolder.isRecorded()) {
                oldObject.set(LoggerHolder.getOldObject());
                newObject.set(LoggerHolder.getNewObject());
            } else {
                throw new RuntimeException("无法记录日志");
            }
        } else {
            newObject.set(SpelUtils.getValue(logger.selectMethod(), selectParam.get()));
        }
        loggerHandler.handleLogger(oldObject.get(), newObject.get(), logger);
    }

    private Object getSelectParam(Logger logger, Object[] args) {
        if (DefaultMethodParameterConverter.class != logger.methodParamConverter()) {
            Converter converter = ClassUtils.getConverterInstance(logger.methodParamConverter());
            return converter.transfor(args);
        }
        Object selectParam = args[logger.paramIndex()];
        selectParam = SpelUtils.getValue(logger.selectParam(), selectParam);
        Converter converter = ClassUtils.getConverterInstance(logger.paramConverter());
        return converter.transfor(selectParam);
    }

}
