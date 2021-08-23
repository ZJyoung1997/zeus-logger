package com.jz.logger.core.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @Author JZ
 * @Date 2021/8/23 11:08
 */
public class SpelUtils implements BeanFactoryAware {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private static final StandardEvaluationContext EVALUATION_CONTEXT = new StandardEvaluationContext();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        EVALUATION_CONTEXT.setBeanResolver(new BeanFactoryResolver(beanFactory));
    }

    public static Object getValue(String expression, Object object) {
        return PARSER.parseExpression(expression).getValue(EVALUATION_CONTEXT, object);
    }

}