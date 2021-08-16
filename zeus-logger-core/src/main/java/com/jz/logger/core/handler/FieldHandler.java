package com.jz.logger.core.handler;

import com.jz.logger.core.FieldInfo;
import com.jz.logger.core.TraceInfo;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.annotation.Trace;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @Author JZ
 * @Date 2021/8/13 17:11
 */
public interface FieldHandler<T> {

    /**
     * @param logger
     * @param trace
     * @param field
     * @param oldObject  {@link Trace} 注解标注的字段的旧值
     * @param newObject  {@link Trace} 注解标注的字段的新值
     * @return
     */
    List<TraceInfo> toFieldInfo(Logger logger, Trace trace, Field field, T oldObject, T newObject);

}
