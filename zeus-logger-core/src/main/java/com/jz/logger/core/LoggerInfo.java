package com.jz.logger.core;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.annotation.Trace;
import com.jz.logger.core.util.ClassUtils;
import lombok.*;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggerInfo {

    private Object oldObject;

    private Object newObject;

    private Logger logger;

    @Getter(AccessLevel.NONE)
    private List<TraceInfo> traceInfos;

    @Setter
    private Date createTime;

    public LoggerInfo(Object oldObject, Object newObject, Logger logger) {
        this.oldObject = oldObject;
        this.newObject = newObject;
        this.logger = logger;
        this.createTime = new Date();
    }

    public List<TraceInfo> getTraceInfos() {
        if (traceInfos == null) {
            List<FieldInfo> fieldInfos = ClassUtils.getTraceFieldInfos(oldObject.getClass());
            traceInfos = fieldInfos.stream()
                    .map(this::buildTraceInfo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return traceInfos;
    }

    @SneakyThrows
    private TraceInfo buildTraceInfo(FieldInfo fieldInfo) {
        Trace trace = fieldInfo.getTrace();
        if (trace.topic().length > 0 && !ArrayUtil.contains(trace.topic(), logger.topic())) {
            return null;
        }
        Field field = fieldInfo.getField();
        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setTag(trace.tag());
        traceInfo.setOrder(trace.order());
        traceInfo.setFieldName(field.getName());
        Object oldValue;
        Object newValue;
        if (CharSequenceUtil.isBlank(trace.targetValue())) {
            field.setAccessible(true);
            oldValue = field.get(oldObject);
            newValue = field.get(newObject);
        } else {
            ExpressionParser parser = new SpelExpressionParser();
            oldValue = parser.parseExpression(trace.targetValue()).getValue(oldObject);
            newValue = parser.parseExpression(trace.targetValue()).getValue(newObject);
        }
        traceInfo.setOldValue(oldValue);
        traceInfo.setNewValue(newValue);
        return traceInfo;
    }

}
