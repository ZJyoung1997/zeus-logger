package com.jz.logger.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggerInfo {

    private Object oldObject;

    private Object newObject;

    private String topic;

    private List<TraceInfo> traceInfos;

    @Setter
    private Date createTime;

    public LoggerInfo(Object oldObject, Object newObject, String topic, List<TraceInfo> traceInfos) {
        this.oldObject = oldObject;
        this.newObject = newObject;
        this.topic = topic;
        this.traceInfos = traceInfos;
        this.createTime = new Date();
    }

}
