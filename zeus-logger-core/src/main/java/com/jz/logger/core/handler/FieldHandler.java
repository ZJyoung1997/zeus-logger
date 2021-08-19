package com.jz.logger.core.handler;

import com.jz.logger.core.FieldInfo;
import com.jz.logger.core.RuntimeFieldInfo;
import com.jz.logger.core.TraceInfo;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.annotation.Trace;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @Author JZ
 * @Date 2021/8/13 17:11
 */
public interface FieldHandler<T> {

    List<TraceInfo> toTraceInfo(RuntimeFieldInfo fieldInfo);

}
