package com.jz.logger.core;

import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.annotation.Trace;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

/**
 * @Author JZ
 * @Date 2021/8/19 13:55
 */
@Getter
public class RuntimeFieldInfo {

    private final Logger logger;

    private final Trace trace;

    private final Field field;

    private String prefix;

    private Object oldObject;

    private Object newObject;

    @Getter(AccessLevel.NONE)
    private Object oldFieldValue;

    @Getter(AccessLevel.NONE)
    private Object newFieldValue;

    @SneakyThrows
    public RuntimeFieldInfo(Logger logger, Trace trace, Field field, Object oldObject, Object newObject, String prefix) {
        Assert.notNull(logger, "Logger cann not be null");
        Assert.notNull(trace, "Trace cann not be null");
        Assert.notNull(field, "Field cann not be null");
        this.logger = logger;
        this.trace = trace;
        this.field = field;
        this.oldObject = oldObject;
        this.newObject = newObject;
        this.prefix = prefix;
        if (oldObject != null) {
            oldFieldValue = field.get(oldObject);
        }
        if (newObject != null) {
            newFieldValue = field.get(newObject);
        }
    }

    public <T> T getNewFieldValue() {
        return (T) newFieldValue;
    }

    public <T> T getOldFieldValue() {
        return (T) oldFieldValue;
    }

}
