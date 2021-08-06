package com.jz.logger.core.aop;

import cn.hutool.core.text.CharSequenceUtil;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.converters.Converter;
import com.jz.logger.core.handler.LoggerHandler;
import com.jz.logger.core.holder.LoggerHolder;
import com.jz.logger.core.util.ClassUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * @Author JZ
 * @Date 2021/8/6 14:20
 */
@Slf4j
public class LoggerAroundAdvice implements MethodBeforeAdvice, AfterReturningAdvice, BeanFactoryAware {

    private final ExpressionParser PARSER = new SpelExpressionParser();

    private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    private final ThreadLocal<Object> oldObject = new ThreadLocal<>();

    private final ThreadLocal<Object> newObject = new ThreadLocal<>();

    private final ThreadLocal<Object> selectParam = new ThreadLocal<>();

    private final LoggerHandler loggerHandler;

    public LoggerAroundAdvice(LoggerHandler loggerHandler) {
        this.loggerHandler = loggerHandler;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
    }

    @Override
    public void before(Method method, Object[] args, Object o) {
        Logger logger = method.getAnnotation(Logger.class);
        newObject.remove();
        oldObject.remove();
        selectParam.remove();
        if (CharSequenceUtil.isBlank(logger.selectMethod())) {
            return;
        }
        selectParam.set(getSelectParam(logger, args));
        oldObject.set(PARSER.parseExpression(logger.selectMethod())
                .getValue(this.evaluationContext, selectParam.get()));
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object o1) {
        Logger logger = method.getAnnotation(Logger.class);
        if (CharSequenceUtil.isBlank(logger.selectMethod())) {
            if (LoggerHolder.isRecorded()) {
                oldObject.set(LoggerHolder.getOldObject());
                newObject.set(LoggerHolder.getNewObject());
            } else {
                throw new RuntimeException("无法记录日志");
            }
        } else {
            newObject.set(PARSER.parseExpression(logger.selectMethod())
                    .getValue(this.evaluationContext, selectParam.get()));
        }
        loggerHandler.handleLogger(oldObject.get(), newObject.get(), logger);
    }

    private Object getSelectParam(Logger logger, Object[] args) {
        Object selectParam = args[logger.paramIndex()];
        Expression expression = PARSER.parseExpression(logger.selectParam());
        selectParam = expression.getValue(selectParam);
        Converter converter = ClassUtils.getConverterInstance(logger.paramConverter());
        return converter.transform(selectParam);
    }

}
