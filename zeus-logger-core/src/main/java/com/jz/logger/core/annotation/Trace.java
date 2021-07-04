package com.jz.logger.core.annotation;

import org.springframework.core.annotation.AliasFor;

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
     * 指定后将在指定topic时生效
     * @return
     */
    String[] topic() default {};

    int order() default 0;

}
