package com.jz.logger.core.util;

import com.jz.logger.core.FieldInfo;
import com.jz.logger.core.annotation.Trace;
import lombok.experimental.UtilityClass;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Field;
import java.util.*;

@UtilityClass
public class ClassUtils {

    private static final Map<Class<?>, List<FieldInfo>> fieldInfoCache = new ConcurrentReferenceHashMap<>();

    public List<FieldInfo> getTraceFieldInfos(Class<?> clazz) {
        List<FieldInfo> classTraces = fieldInfoCache.get(clazz);
        if (classTraces == null) {
            synchronized (fieldInfoCache) {
                classTraces = fieldInfoCache.get(clazz);
                if (classTraces == null) {
                    classTraces = fieldInfoCache.computeIfAbsent(clazz, e -> new ArrayList<>());
                    for (Field field : clazz.getDeclaredFields()) {
                        Trace trace = field.getAnnotation(Trace.class);
                        if (trace != null) {
                            classTraces.add(new FieldInfo(trace, field));
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableList(classTraces);
    }

}
