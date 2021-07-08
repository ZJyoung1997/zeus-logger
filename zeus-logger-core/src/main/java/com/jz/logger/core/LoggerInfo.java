package com.jz.logger.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.annotation.Trace;
import com.jz.logger.core.util.ClassUtils;
import lombok.*;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggerInfo {

    private Logger logger;

    private Object oldObject;

    private Object newObject;

    private Map<String, Object> extDataMap;

    @Getter(AccessLevel.NONE)
    private List<List<TraceInfo>> multipleTraceInfos;

    @Setter
    private Date createTime;

    public LoggerInfo(Object oldObject, Object newObject, Map<String, Object> extDataMap, Logger logger) {
        this.oldObject = oldObject;
        this.newObject = newObject;
        this.extDataMap = extDataMap;
        this.logger = logger;
        this.createTime = new Date();
    }

    public List<List<TraceInfo>> getTraceInfos() {
        if (multipleTraceInfos != null) {
            return multipleTraceInfos;
        } else if (oldObject == null && newObject == null) {
            multipleTraceInfos = Collections.emptyList();
        } else if (oldObject instanceof Collection || newObject instanceof Collection) {
            Object[] oldObjectArray = oldObject == null ? new Object[0] : ((Collection) oldObject).toArray();
            Object[] newObjectArray = newObject == null ? new Object[0] : ((Collection) newObject).toArray();
            int length = Math.max(oldObjectArray.length, newObjectArray.length);
            multipleTraceInfos = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                Object oldObject = oldObjectArray[i];
                Object newObject = newObjectArray[i];
                Class<?> clazz = oldObject != null ? oldObject.getClass() :
                        (newObject != null ? newObject.getClass() : null);
                multipleTraceInfos.add(ClassUtils.getTraceFieldInfos(clazz).stream()
                        .map(e -> buildTraceInfo(e, oldObject, newObject))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            }
        } else {
            Class<?> clazz = oldObject != null ? oldObject.getClass() : newObject.getClass();
            if (clazz == null) {
                multipleTraceInfos = Collections.emptyList();
            } else {
                multipleTraceInfos = new ArrayList<>(1);
                multipleTraceInfos.add(ClassUtils.getTraceFieldInfos(clazz).stream()
                        .map(e -> buildTraceInfo(e, oldObject, newObject))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            }
        }
        return multipleTraceInfos;
    }

    @SneakyThrows
    private TraceInfo buildTraceInfo(FieldInfo fieldInfo, Object oldObject, Object newObject) {
        Trace trace = fieldInfo.getTrace();
        if (trace.topic().length > 0 && !ArrayUtil.contains(trace.topic(), logger.topic())) {
            return null;
        }
        Field field = fieldInfo.getField();
        field.setAccessible(true);
        Object oldValue = oldObject == null ? null : field.get(oldObject);
        Object newValue= newObject == null ? null : field.get(newObject);
        if (CharSequenceUtil.isNotBlank(trace.targetValue())) {
            ExpressionParser parser = new SpelExpressionParser();
            oldValue = parser.parseExpression(trace.targetValue()).getValue(oldValue);
            newValue = parser.parseExpression(trace.targetValue()).getValue(newValue);
        }
        if (isEqual(oldObject, newObject)) {
            return null;
        }
        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setOldValue(oldValue);
        traceInfo.setNewValue(newValue);
        traceInfo.setTag(trace.tag());
        traceInfo.setOrder(trace.order());
        traceInfo.setFieldName(field.getName());
        return traceInfo;
    }

    private boolean isEqual(Object oldObject, Object newObject) {
        if (oldObject != null && oldObject.equals(newObject)) {
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

}
