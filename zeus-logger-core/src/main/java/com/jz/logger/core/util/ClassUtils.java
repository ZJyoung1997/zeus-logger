package com.jz.logger.core.util;

import com.jz.logger.core.TraceInfo;
import com.jz.logger.core.annotation.Trace;
import lombok.experimental.UtilityClass;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Field;
import java.util.*;

@UtilityClass
public class ClassUtils {

    private static final Map<Class<?>, List<TraceInfo>> traceInfoCache = new ConcurrentReferenceHashMap<>();

    public List<TraceInfo> getTraceInfo(Class<?> clazz) {
        List<TraceInfo> traceInfos = traceInfoCache.get(clazz);
        if (traceInfos == null) {
            synchronized (traceInfoCache) {
                traceInfos = traceInfoCache.get(clazz);
                if (traceInfos == null) {
                    traceInfos = traceInfoCache.computeIfAbsent(clazz, e -> new ArrayList<>());
                    for (Field field : clazz.getDeclaredFields()) {
                        Trace trace = field.getAnnotation(Trace.class);
                        if (trace != null) {
                            traceInfos.add(TraceInfo.build(trace, field.getName()));
                        }
                    }
                    traceInfos.sort(Comparator.comparingInt(TraceInfo::getOrder));
                }
            }
        }
        return Collections.unmodifiableList(traceInfos);
    }

}
