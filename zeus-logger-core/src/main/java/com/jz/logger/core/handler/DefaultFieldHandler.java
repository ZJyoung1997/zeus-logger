package com.jz.logger.core.handler;

import com.jz.logger.core.RuntimeFieldInfo;
import com.jz.logger.core.TraceInfo;

import java.util.List;

/**
 * @Author JZ
 * @Date 2021/8/13 17:16
 */
public class DefaultFieldHandler implements FieldHandler<Object> {

    @Override
    public List<TraceInfo> toTraceInfo(RuntimeFieldInfo fieldInfo) {
        return null;
    }

}
