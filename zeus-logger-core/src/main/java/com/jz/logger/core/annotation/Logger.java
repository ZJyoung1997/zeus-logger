package com.jz.logger.core.annotation;

import com.jz.logger.core.enumerate.Strategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Logger {

    String selectMethod() default "";

    String selectParam() default "#root";

    String handlerBeanName() default "defaultLoggerTraceHandler";

    String topic() default "";

    Strategy strategy() default Strategy.ASYN_SERIAL;

    int paramIndex() default 0;

}
