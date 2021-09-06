package com.jz.logger.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.jz.logger.core.FieldInfo;
import com.jz.logger.core.TraceInfo;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.annotation.Trace;
import com.jz.logger.core.converters.Converter;
import com.jz.logger.core.converters.DefaultConverter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @Author JZ
 * @Date 2021/7/30 16:31
 */
@UtilityClass
public class LoggerUtils {

    /**
     *
     * @param logger
     * @param trace          当前处理字段上的 trace
     * @param fieldName      当前处理字段名称
     * @param oldObject      当前处理字段的旧值
     * @param newObject      当前处理字段的新值
     * @return
     */
    public TraceInfo buildTraceInfo(int resourceType, Logger logger, Trace trace, String fieldName, Object oldObject, Object newObject) {
        if (!LoggerUtils.isMatch(resourceType, logger.operationType(), logger.topic(), trace)) {
            return null;
        }
        oldObject = transforValue(oldObject, trace);
        newObject = transforValue(newObject, trace);
        if (isEqual(oldObject, newObject)) {
            return null;
        }
        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setTag(trace.tag());
        traceInfo.setOrder(trace.order());
        traceInfo.setFieldName(fieldName);
        traceInfo.setOldValue(oldObject);
        traceInfo.setNewValue(newObject);
        return traceInfo;
    }

    /**
     *
     * @param logger
     * @param fieldInfo      当前处理字段的信息
     * @param oldObject      当前处理字段所属的旧对象
     * @param newObject      当前处理字段所属的新对象
     * @return
     */
    @SneakyThrows
    public TraceInfo buildTraceInfo(int resourceType, Logger logger, FieldInfo fieldInfo, Object oldObject, Object newObject) {
        Trace trace = fieldInfo.getTrace();
        if (!LoggerUtils.isMatch(resourceType, logger.operationType(), logger.topic(), trace)) {
            return null;
        }
        Field field = fieldInfo.getField();
        field.setAccessible(true);
        Object oldFieldValue = oldObject == null ? null : field.get(oldObject);
        Object newFieldValue= newObject == null ? null : field.get(newObject);
        return buildTraceInfo(resourceType, logger, fieldInfo.getTrace(), field.getName(), oldFieldValue, newFieldValue);
    }

    public Object transforValue(Object value, Trace trace) {
        if (CharSequenceUtil.isNotBlank(trace.targetValue())) {
            value = SpelUtils.getValue(trace.targetValue(), value);
        }
        Converter converter = ClassUtils.getConverterInstance(trace.converter());
        if (converter instanceof DefaultConverter) {
            return value;
        } else {
            return converter.transfor(value);
        }
    }

    public boolean isEqual(Object oldObject, Object newObject) {
        if (oldObject == newObject) {
            return true;
        } else if (oldObject != null && oldObject.equals(newObject)) {
            return true;
        } else if (newObject != null && newObject.equals(oldObject)) {
            return true;
        } else if (oldObject instanceof Collection && newObject instanceof Collection) {
            Collection oldCollection = (Collection) oldObject;
            Collection newCollection = (Collection) newObject;
            if (oldCollection.size() == newCollection.size() &&
                    CollUtil.containsAll(oldCollection, newCollection)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMatch(int resourceType, int operationType, String topic,Trace trace) {
        if (trace.topic().length > 0 && !ArrayUtil.contains(trace.topic(), topic)) {
            return false;
        }
        if (trace.resourceType().length > 0 && !ArrayUtil.contains(trace.resourceType(), resourceType)) {
            return false;
        }
        if (trace.operationType().length > 0 && !ArrayUtil.contains(trace.operationType(), operationType)) {
            return false;
        }
        return true;
    }

    public static int getResourceType(Object o1, Object o2, int resourceType, String spel) {
        if (CharSequenceUtil.isBlank(spel)) {
            return resourceType;
        }
        Object effectiveObj = o1 != null ? o1 : o2;
        if (effectiveObj != null) {
            return (int) SpelUtils.getValue(spel, effectiveObj);
        }
        return resourceType;
    }

}
