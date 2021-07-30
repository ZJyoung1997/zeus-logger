package com.jz.logger.core.annotation;

import com.jz.logger.core.enumerate.Strategy;
import com.jz.logger.core.LoggerExtensionData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author JZ
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Logger {

    String topic() default "";

    /**
     * 值为spel表达式，例：单个对象参数 #root.id、集合参数 #root?.peopleList?.![id]
     * 获取到的值将作为 {@link #selectMethod()} 方法的入参
     */
    String selectParam() default "#root";

    /**
     * 值为spel表达式，该表达式的结果将作为目标对象的新旧快照
     * 例：
     * 若方法有入参则只支持单个入参，其值为 #{@link #selectParam()} 的结果，@beanName.get(#root)
     * 若方法无参，@beanName.get()
     */
    String selectMethod() default "";

    /**
     * 自定义扩展数据，需实现 {@link LoggerExtensionData} 接口
     */
    Class[] customExtData() default {};

    /**
     * 禁用全局扩展数据
     */
    boolean disableGlobalExtData() default false;

    String handlerBeanName() default "defaultLoggerTraceHandler";

    Strategy strategy() default Strategy.DEFAULT;

    /**
     * 资源类型
     */
    int resourceType() default 0;

    int paramIndex() default 0;

}
