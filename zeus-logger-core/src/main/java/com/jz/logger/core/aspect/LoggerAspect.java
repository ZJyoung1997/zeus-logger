package com.jz.logger.core.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import com.jz.logger.core.LoggerInfo;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.enumerate.Strategy;
import com.jz.logger.core.event.LoggerEventProvider;
import com.jz.logger.core.handler.LoggerTraceHandler;
import com.jz.logger.core.holder.LoggerHolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LoggerAspect implements BeanFactoryAware {

    private static final String DEFAULT_LOGGER_TRACE_HANDLER_BEAN_NAME = "defaultLoggerTraceHandler";

    private final ExpressionParser PARSER = new SpelExpressionParser();

    private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    private final Map<String, LoggerTraceHandler> loggerTraceHandlerCache = new ConcurrentReferenceHashMap<>();

    private BeanFactory beanFactory;

    private final LoggerEventProvider loggerEventProvider;

    public LoggerAspect(LoggerEventProvider loggerEventProvider) {
        this.loggerEventProvider = loggerEventProvider;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        this.evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        loggerTraceHandlerCache.put(DEFAULT_LOGGER_TRACE_HANDLER_BEAN_NAME, beanFactory.getBean(DEFAULT_LOGGER_TRACE_HANDLER_BEAN_NAME, LoggerTraceHandler.class));
    }

    @SneakyThrows
    @Around("@annotation(com.jz.logger.core.annotation.Logger)")
    public Object around(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Object rawObject = joinPoint.getTarget();
        Method method = rawObject.getClass().getDeclaredMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        Logger logger = method.getAnnotation(Logger.class);

        Object methodResult;
        Object oldObject = null;
        Object newObject = null;
        if (CharSequenceUtil.isNotBlank(logger.selectMethod())) {
            Object selectParam = getSelectParam(logger, joinPoint.getArgs());
            oldObject = PARSER.parseExpression(logger.selectMethod())
                    .getValue(this.evaluationContext, selectParam);
            methodResult = joinPoint.proceed();
            newObject = PARSER.parseExpression(logger.selectMethod())
                    .getValue(this.evaluationContext, selectParam);
        } else {
            methodResult = joinPoint.proceed();
            if (LoggerHolder.isRecorded()) {
                oldObject  = LoggerHolder.getOldObject();
                newObject  = LoggerHolder.getNewObject();
            } else {
                throw new RuntimeException("无法记录日志");
            }
        }

        loggerHandle(oldObject, newObject, logger);
        return methodResult;
    }

    private void loggerHandle(Object oldObject, Object newObject, Logger logger) {
        LoggerTraceHandler loggerTraceHandler = getLoggerTraceHandler(logger.handlerBeanName());
        Strategy strategy = logger.strategy();
        LoggerInfo loggerInfo = new LoggerInfo(oldObject, newObject, logger);
        if (Strategy.SYNC == strategy) {
            loggerTraceHandler.execute(loggerInfo);
        } else if (Strategy.ASYN_SERIAL == strategy) {
            loggerEventProvider.publishWithSerial(loggerInfo, loggerTraceHandler);
        } else if (Strategy.ASYN_CONCURRENT == strategy) {
            loggerEventProvider.publishWithConcurrent(loggerInfo, loggerTraceHandler);
        }
    }

    public LoggerTraceHandler getLoggerTraceHandler(String loggerHandlerBeanName) {
        LoggerTraceHandler loggerTraceHandler = loggerTraceHandlerCache.get(loggerHandlerBeanName);
        if (loggerTraceHandler == null) {
            loggerTraceHandler = beanFactory.getBean(loggerHandlerBeanName, LoggerTraceHandler.class);
            loggerTraceHandlerCache.put(loggerHandlerBeanName, loggerTraceHandler);
        }
        return loggerTraceHandler;
    }

    private Object getSelectParam(Logger logger, Object[] args) {
        Object selectParam = args[logger.paramIndex()];
        Expression expression = PARSER.parseExpression(logger.selectParam());
        selectParam = expression.getValue(selectParam);
        return selectParam;
    }

}
