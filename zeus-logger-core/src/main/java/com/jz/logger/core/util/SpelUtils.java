package com.jz.logger.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * @Author JZ
 * @Date 2021/8/23 11:08
 */
@Component
@UtilityClass
public class SpelUtils {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private static final StandardEvaluationContext EVALUATION_CONTEXT = new StandardEvaluationContext();

    @Autowired
    private void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        EVALUATION_CONTEXT.setBeanResolver(new BeanFactoryResolver(beanFactory));
    }

    public Object getValue(String expression, Object object) {
        return PARSER.parseExpression(expression).getValue(EVALUATION_CONTEXT, object);
    }

}
