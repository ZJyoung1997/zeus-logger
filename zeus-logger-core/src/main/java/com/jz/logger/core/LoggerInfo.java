package com.jz.logger.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.annotation.Trace;
import com.jz.logger.core.handler.DefaultFieldHandler;
import com.jz.logger.core.handler.FieldHandler;
import com.jz.logger.core.matcher.Matcher;
import com.jz.logger.core.util.ClassUtils;
import com.jz.logger.core.util.CollectionUtils;
import com.jz.logger.core.util.LoggerUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggerInfo {

    private Logger logger;

    private Object oldObject;

    private Object newObject;

    private Map<String, Object> extDataMap;

    @Getter(AccessLevel.NONE)
    private List<List<TraceInfo>> multipleTraceInfos;

    /**
     * 日志生成时的时间戳
     */
    private Long createTime;

    public LoggerInfo(Object oldObject, Object newObject, Map<String, Object> extDataMap, Logger logger) {
        this.oldObject = oldObject;
        this.newObject = newObject;
        this.extDataMap = extDataMap;
        this.logger = logger;
        this.createTime = System.currentTimeMillis();
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
                Object oldObject = oldObjectArray.length <= i ? null : oldObjectArray[i];
                Object newObject = newObjectArray.length <= i ? null : newObjectArray[i];
                multipleTraceInfos.add(getTraceInfos(oldObject, newObject));
            }
        } else {
            multipleTraceInfos = new ArrayList<>(1);
            multipleTraceInfos.add(getTraceInfos(oldObject, newObject));
        }
        return multipleTraceInfos;
    }

    @SneakyThrows
    private List<TraceInfo> getTraceInfos(Object oldObject, Object newObject) {
        if (oldObject == null && newObject == null) {
            return Collections.emptyList();
        }
        Class<?> clazz = oldObject != null ? oldObject.getClass() : newObject.getClass();
        if (clazz == null) {
            return Collections.emptyList();
        }
        List<TraceInfo> result = new ArrayList<>();
        List<FieldInfo> fieldInfos = ClassUtils.getTraceFieldInfos(clazz);
        for (FieldInfo fieldInfo : fieldInfos) {
            Trace trace = fieldInfo.getTrace();
            if (DefaultFieldHandler.class != trace.fieldHandler()) {
                FieldHandler fieldHandler = ClassUtils.getFieldHandlerInstance(trace.fieldHandler());
                Field field = fieldInfo.getField();
                field.setAccessible(true);
                Object oldFieldValue = oldObject == null ? null : field.get(oldObject);
                Object newFieldValue = newObject == null ? null : field.get(newObject);
                List<TraceInfo> fieldHandlerResult = fieldHandler.toFieldInfo(logger, trace, field, oldFieldValue, newFieldValue);
                if (CollUtil.isNotEmpty(fieldHandlerResult)) {
                    result.addAll(fieldHandlerResult);
                }
            } else if (trace.permeate()) {
                Matcher matcher = ClassUtils.getMatcherInstance(trace.collElementMatcher());
                Field field = fieldInfo.getField();
                field.setAccessible(true);
                Object oldFieldValue = oldObject == null ? null : field.get(oldObject);
                Object newFieldValue = newObject == null ? null : field.get(newObject);
                if (oldFieldValue instanceof Collection || newFieldValue instanceof Collection) {
                    Collection<?> oldCollection = oldFieldValue == null ? Collections.emptyList() : ListUtil.toList((Collection<?>) oldFieldValue);
                    Collection<?> newCollection = newFieldValue == null ? Collections.emptyList() : ListUtil.toList((Collection<?>) newFieldValue);
                    for (Object newElement : newCollection) {
                        Object oldElement = CollectionUtils.findFirst(oldCollection, newElement, matcher);
                        if (oldElement == null) {
                            // 旧集合中没找到新的，说明该元素是新增的
                            result.add(LoggerUtils.buildTraceInfo(logger, trace, field.getName(), null, newElement));
                        } else {
                            result.addAll(getTraceInfos(oldElement, newElement));
                        }
                    }
                    for (Object oldElement : oldCollection) {
                        Object newElement = CollectionUtils.findFirst(newCollection, oldElement, matcher);
                        if (newElement == null) {
                            // 新集合中没找到旧的，说明该元素被删掉了
                            result.add(LoggerUtils.buildTraceInfo(logger, trace, field.getName(), null, oldElement));
                        }
                    }
                } else {
                    result.addAll(getTraceInfos(oldFieldValue, newFieldValue));
                }
            } else {
                TraceInfo traceInfo = LoggerUtils.buildTraceInfo(logger, fieldInfo, oldObject, newObject);
                if (traceInfo != null) {
                    result.add(traceInfo);
                }
            }
        }
        return result;
    }

}
