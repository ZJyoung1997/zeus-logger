package com.jz.logger.core.aop;

import cn.hutool.core.text.CharSequenceUtil;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.converters.Converter;
import com.jz.logger.core.handler.LoggerHandler;
import com.jz.logger.core.holder.LoggerHolder;
import com.jz.logger.core.util.ClassUtils;
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

import java.lang.reflect.Method;

@Slf4j
public class LoggerAspect implements BeanFactoryAware {

    private final ExpressionParser PARSER = new SpelExpressionParser();

    private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    private final LoggerHandler loggerHandler;

    public LoggerAspect(LoggerHandler loggerHandler) {
        this.loggerHandler = loggerHandler;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
    }

    @SneakyThrows
    @Around("@annotation(com.jz.logger.core.annotation.Logger)")
    public Object around(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Object rawObject = joinPoint.getTarget();
        Method method = rawObject.getClass().getDeclaredMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        Logger logger = method.getAnnotation(Logger.class);

        Object methodResult;
        Object oldObject;
        Object newObject;
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
        loggerHandler.handleLogger(oldObject, newObject, logger);
        return methodResult;
    }

    private Object getSelectParam(Logger logger, Object[] args) {
        Object selectParam = args[logger.paramIndex()];
        Expression expression = PARSER.parseExpression(logger.selectParam());
        selectParam = expression.getValue(selectParam);
        Converter converter = ClassUtils.getConverterInstance(logger.paramConverter());
        return converter.transform(selectParam);
    }

}
