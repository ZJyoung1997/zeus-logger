package com.jz.logger.core.annotation;

import com.jz.logger.core.TraceInfo;
import com.jz.logger.core.converters.Converter;
import com.jz.logger.core.converters.DefaultConverter;

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

    /**
     * 该 trace 的 标签
     */
    String tag() default "";

    /**
     * 该属性值应为spel表达式
     * 该属性用于提取字段中指定字段的值
     */
    String targetValue() default "";

    /**
     * 指定后将在指定 {@link Logger#topic} 时生效
     */
    String[] topic() default {};

    /**
     * 指定后将在指定 {@link Logger#resourceType} 时生效
     */
    int[] resourceType() default {};

    /**
     * 指定后将在指定 {@link Logger#operationType()} 时生效
     */
    int[] operationType() default {};

    /**
     * 转换器，用于对 {@link TraceInfo#oldValue} 和 {@link TraceInfo#newValue} 的转化
     */
    Class<? extends Converter> converter() default DefaultConverter.class;

    int order() default 0;

}
