package com.jz.logger.core;

import cn.hutool.core.text.CharSequenceUtil;
import com.jz.logger.core.annotation.Trace;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TraceInfo {

    private String fieldName;

    private String tag;

    private int order;

    public static TraceInfo build(Trace trace, String fieldName) {
        TraceInfo traceInfo = new TraceInfo();
        traceInfo.fieldName = fieldName;
        traceInfo.order = trace.order();
        if (CharSequenceUtil.isNotBlank(trace.tag())) {
            traceInfo.tag = trace.tag();
        }
        return traceInfo;
    }

}
