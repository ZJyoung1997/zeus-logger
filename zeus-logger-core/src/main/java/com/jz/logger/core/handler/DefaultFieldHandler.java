package com.jz.logger.core.handler;

import com.jz.logger.core.TraceInfo;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.annotation.Trace;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @Author JZ
 * @Date 2021/8/13 17:16
 */
public class DefaultFieldHandler implements FieldHandler<Object> {

    @Override
    public List<TraceInfo> toFieldInfo(Logger logger, Trace trace, Field field, Object oldObject, Object newObject) {
        return null;
    }

}
