package com.jz.logger.core.util;

import cn.hutool.core.collection.ListUtil;
import lombok.experimental.UtilityClass;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class MethodUtils {

    private static final Map<Method, ConcurrentHashMap<Class<? extends Annotation>, Annotation>> METHOD_ANNOTATION_CACHE = new ConcurrentReferenceHashMap<>();

    public <A extends Annotation> A getMethodAnnotation(Method method, Class<A> annotationClass) {
        if (method == null || annotationClass == null) {
            return null;
        }
        Map<Class<? extends Annotation>, Annotation> classAnnotationMap = METHOD_ANNOTATION_CACHE.computeIfAbsent(method, e -> new ConcurrentHashMap<>());
        A targetAnnotation = (A) classAnnotationMap.get(annotationClass);
        if (targetAnnotation != null) {
            return targetAnnotation;
        }
        String methodName = method.getName();
        Class<?>[] methodParamTypes = method.getParameterTypes();
        Class<?> clazz = method.getDeclaringClass();
        while (clazz != null && targetAnnotation == null) {
            Method targetMethod;
            try {
                targetMethod = clazz.getDeclaredMethod(methodName, methodParamTypes);
                targetAnnotation = targetMethod.getAnnotation(annotationClass);
            } catch (NoSuchMethodException e) {
            }
            if (targetAnnotation == null) {
                Class<?>[] allInterfaces = getAllInterfaces(clazz, true);
                for (int i = 0; i < allInterfaces.length && targetAnnotation == null; i++) {
                    try {
                        targetMethod = allInterfaces[i].getMethod(methodName, methodParamTypes);
                        targetAnnotation = targetMethod.getAnnotation(annotationClass);
                    } catch (NoSuchMethodException e) {
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return targetAnnotation;
    }

    private Class<?>[] getAllInterfaces(Class<?> clazz, boolean searchSupers) {
        return getAllInterfaceList(clazz, searchSupers).toArray(new Class<?>[0]);
    }

    private List<Class<?>> getAllInterfaceList(Class<?> clazz, boolean searchSupers) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        Class<?>[] allInterfaces = clazz.getInterfaces();
        Set<Class<?>> allInterfaceSet = new LinkedHashSet<>();
        allInterfaceSet.add(clazz);
        if (!searchSupers) {
            return ListUtil.toList(allInterfaceSet);
        }
        for (Class<?> interfaceClass : allInterfaces) {
            if (allInterfaceSet.add(interfaceClass)) {
                allInterfaceSet.addAll(ListUtil.toList(getAllInterfaces(interfaceClass, searchSupers)));
            }
        }
        return ListUtil.toList(allInterfaces);
    }

}
