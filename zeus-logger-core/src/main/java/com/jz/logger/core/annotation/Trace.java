package com.jz.logger.core.annotation;

import org.springframework.core.annotation.AliasFor;

public @interface Trace {

    @AliasFor("tag")
    String value() default "";

    @AliasFor("value")
    String tag() default "";

    int order() default 0;

}
