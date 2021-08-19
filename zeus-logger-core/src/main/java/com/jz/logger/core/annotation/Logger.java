package com.jz.logger.core.annotation;

import com.jz.logger.core.LoggerExtensionData;
import com.jz.logger.core.converters.Converter;
import com.jz.logger.core.converters.DefaultConverter;
import com.jz.logger.core.converters.DefaultMethodParameterConverter;
import com.jz.logger.core.converters.MethodParameterConverter;
import com.jz.logger.core.enumerate.Strategy;
import com.jz.logger.core.handler.DefaultLoggerTraceHandler;
import com.jz.logger.core.handler.LoggerTraceHandler;
import com.jz.logger.core.matcher.DefaultMatcher;
import com.jz.logger.core.matcher.Matcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

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
     * 将 {@link #selectParam()} 获取到的值进行转换
     */
    Class<? extends Converter> paramConverter() default DefaultConverter.class;

    /**
     * 对 Logger 注解标注的方法的所有入参进行转换，
     * 若定义该转换器，则 {@link #selectParam()}、{@link #paramConverter()}、{@link #paramIndex()} 将无效
     */
    Class<? extends MethodParameterConverter> methodParamConverter() default DefaultMethodParameterConverter.class;

    /**
     * 值为spel表达式，该表达式的结果将作为目标对象的新旧快照。
     * 当 {@link #methodParamConverter()} 为默认值时，方法入参为 #{@link #selectParam()} 的结果，
     * 只支持单个入参，@beanName.get(#root) ，若方法无参，@beanName.get()。
     *
     * 当 {@link #methodParamConverter()} 不为默认值时，方法的入参为 #{@link #methodParamConverter()} 的结果，
     * 此时方法可以支持多个入参，但其spel表达式要支持，例如：@mapper.get(#root[0], #root[1])
     */
    String selectMethod() default "";

    /**
     * 当 {@link #selectMethod()} 的返回值类型为 {@link Collection} 时，用于匹配新旧集合中元素是否为同一个
     */
    Class<? extends Matcher> collElementMatcher() default DefaultMatcher.class;

    /**
     * 自定义扩展数据，需实现 {@link LoggerExtensionData} 接口
     */
    Class<? extends LoggerExtensionData>[] customExtData() default {};

    /**
     * 禁用全局扩展数据
     */
    boolean disableGlobalExtData() default false;

    /**
     * 指定 LoggerTraceHandler 处理器
     * @return
     */
    Class<? extends LoggerTraceHandler> traceHandler() default DefaultLoggerTraceHandler.class;

    Strategy strategy() default Strategy.DEFAULT;

    /**
     * 资源类型
     */
    int resourceType() default -1;

    /**
     * 操作类型
     */
    int operationType() default -1;

    int paramIndex() default 0;

    /**
     * 是否开启手动记录日志，true 开启、false 关闭
     */
    boolean enabledManual() default false;

    /**
     * 重试次数，默认-1，使用配置的默认次数 {@link com.jz.logger.autoconfig.LoggerProperties#defaultRetryTimes}，
     * 若为其他负数，则代表该日志记录失败后不进行重试
     */
    int retryTimes() default -1;

    /**
     * 需要重试的异常
     */
    Class<? extends Throwable>[] retryFor() default {};

    /**
     * 不需要重试的异常
     */
    Class<? extends Throwable>[] noRetryFor() default {};

}
