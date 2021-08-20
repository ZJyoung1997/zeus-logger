package com.jz.logger.core;

import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.annotation.Trace;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

/**
 * @Author JZ
 * @Date 2021/8/19 13:55
 */
@Setter
@Getter
public class RuntimeFieldInfo {

    private final Logger logger;

    private final Trace trace;

    private final Field field;

    private String prefix;

    @Getter(AccessLevel.NONE)
    private Object oldObject;

    @Getter(AccessLevel.NONE)
    private Object newObject;

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
    }

    public <T> T getNewObject() {
        return (T) newObject;
    }

    public <T> T getOldObject() {
        return (T) oldObject;
    }

}
