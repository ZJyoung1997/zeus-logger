package com.jz.logger.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jz
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trace {

    @AliasFor("tag")
    String value() default "";

    @AliasFor("value")
    String tag() default "";

    /**
     * 该属性值应为spel表达式
     * 该属性用于提取字段中指定字段的值
     * @return
     */
    String targetValue() default "";

    /**
     * 指定后将在指定 {@link Logger#topic} 时生效
     * @return
     */
    String[] topic() default {};

    /**
     * 指定后将在指定 {@link Logger#resourceType} 时生效
     * @return
     */
    int[] resourceType() default {};

    int order() default 0;

}
