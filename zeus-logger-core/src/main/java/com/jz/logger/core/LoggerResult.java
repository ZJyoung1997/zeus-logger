package com.jz.logger.core;

import lombok.Getter;

import java.util.List;

/**
 * @Author JZ
 * @Date 2021/8/17 18:42
 */
@Getter
public class LoggerResult {

    private final Object oldObject;

    private final Object newObject;

    private final int resourceType;

    private List<TraceInfo> traceInfos;

    public LoggerResult(Object oldObject, Object newObject, int resourceType, List<TraceInfo> traceInfos) {
        this.oldObject = oldObject;
        this.newObject = newObject;
        this.resourceType = resourceType;
        this.traceInfos = traceInfos;
    }

}
