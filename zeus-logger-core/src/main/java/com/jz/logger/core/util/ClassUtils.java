package com.jz.logger.core.util;

import com.jz.logger.core.FieldInfo;
import com.jz.logger.core.annotation.Trace;
import com.jz.logger.core.converters.Converter;
import com.jz.logger.core.handler.FieldHandler;
import com.jz.logger.core.matcher.Matcher;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@UtilityClass
public class ClassUtils {

    private static final Map<Class<?>, List<FieldInfo>> FIELD_INFO_CACHE = new ConcurrentReferenceHashMap<>();

    /**
     * 转换器实例缓存
     */
    private static final Map<Class<?>, Converter<?, ?>> CONVERTER_CACHE = new ConcurrentReferenceHashMap<>();

    /**
     * 匹配器实例缓存
     */
    private static final Map<Class<?>, Matcher<?>> MATCHER_CACHE = new ConcurrentReferenceHashMap<>();

    /**
     * FieldHandler实例缓存
     */
    private static final Map<Class<?>, FieldHandler<?>> FIELD_HANDLER_CACHE = new ConcurrentReferenceHashMap<>();

    @SneakyThrows
    public Converter<?, ?> getConverterInstance(Class<? extends Converter> clazz) {
        Converter<?, ?> converter = CONVERTER_CACHE.get(clazz);
        if (converter != null) {
            return converter;
        }
        converter = (Converter<?, ?>) clazz.getDeclaredConstructor().newInstance();
        CONVERTER_CACHE.put(clazz, converter);
        return converter;
    }

    @SneakyThrows
    public Matcher<?> getMatcherInstance(Class<? extends Matcher> clazz) {
        Matcher<?> matcher = MATCHER_CACHE.get(clazz);
        if (matcher != null) {
            return matcher;
        }
        matcher = (Matcher<?>) clazz.getDeclaredConstructor().newInstance();
        MATCHER_CACHE.put(clazz, matcher);
        return matcher;
    }

    @SneakyThrows
    public FieldHandler<?> getFieldHandlerInstance(Class<? extends FieldHandler> clazz) {
        FieldHandler<?> fieldHandler = FIELD_HANDLER_CACHE.get(clazz);
        if (fieldHandler != null) {
            return fieldHandler;
        }
        fieldHandler = (FieldHandler<?>) clazz.getDeclaredConstructor().newInstance();
        FIELD_HANDLER_CACHE.put(clazz, fieldHandler);
        return fieldHandler;
    }

    public List<FieldInfo> getTraceFieldInfos(Class<?> clazz) {
        List<FieldInfo> classTraces = FIELD_INFO_CACHE.get(clazz);
        if (classTraces == null) {
            synchronized (FIELD_INFO_CACHE) {
                classTraces = FIELD_INFO_CACHE.get(clazz);
                if (classTraces == null) {
                    classTraces = new ArrayList<>();
                    Class<?> superclass = clazz;
                    while (superclass != Object.class) {
                        for (Field field : superclass.getDeclaredFields()) {
                            Trace trace = field.getAnnotation(Trace.class);
                            if (trace != null && classTraces.stream()
                                    .noneMatch(e -> e.getField().getName().equals(field.getName()))) {
                                classTraces.add(new FieldInfo(trace, field));
                            }
                        }
                        superclass = superclass.getSuperclass();
                    }
                    if (classTraces.isEmpty()) {
                        FIELD_INFO_CACHE.put(clazz, Collections.emptyList());
                    } else {
                        FIELD_INFO_CACHE.put(clazz, classTraces);
                    }
                }
            }
        }
        return Collections.unmodifiableList(classTraces);
    }

    /**
     * 判断 originClazz 是否实现了 interfaceClazz 接口
     */
    public boolean hasInterface(@NonNull Class<?> originClazz, @NonNull Class<?> interfaceClazz) {
        for (Class<?> anInterface : originClazz.getInterfaces()) {
            if (anInterface == interfaceClazz) {
                return true;
            }
        }
        return false;
    }

}
