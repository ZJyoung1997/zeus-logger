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
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @Author JZ
 * @Date 2021/7/30 16:31
 */
@UtilityClass
public class LoggerUtils {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public TraceInfo buildTraceInfo(Logger logger, Trace trace, String fieldName, Object oldObject, Object newObject) {
        if (!LoggerUtils.isMatch(logger, trace)) {
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

    @SneakyThrows
    public TraceInfo buildTraceInfo(Logger logger, FieldInfo fieldInfo, Object oldObject, Object newObject) {
        Trace trace = fieldInfo.getTrace();
        if (!LoggerUtils.isMatch(logger, trace)) {
            return null;
        }
        Field field = fieldInfo.getField();
        field.setAccessible(true);
        Object oldValue = oldObject == null ? null : field.get(oldObject);
        Object newValue= newObject == null ? null : field.get(newObject);
        oldValue = transforValue(oldValue, trace);
        newValue = transforValue(newValue, trace);
        if (isEqual(oldValue, newValue)) {
            return null;
        }
        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setTag(trace.tag());
        traceInfo.setOrder(trace.order());
        traceInfo.setFieldName(field.getName());
        traceInfo.setOldValue(oldValue);
        traceInfo.setNewValue(newValue);
        return traceInfo;
    }

    public Object transforValue(Object value, Trace trace) {
        if (CharSequenceUtil.isNotBlank(trace.targetValue())) {
            value = PARSER.parseExpression(trace.targetValue()).getValue(value);
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

    public boolean isMatch(Logger logger, Trace trace) {
        if (trace.topic().length > 0 && !ArrayUtil.contains(trace.topic(), logger.topic())) {
            return false;
        }
        if (trace.resourceType().length > 0 && !ArrayUtil.contains(trace.resourceType(), logger.resourceType())) {
            return false;
        }
        if (trace.operationType().length > 0 && !ArrayUtil.contains(trace.operationType(), logger.operationType())) {
            return false;
        }
        return true;
    }

}
