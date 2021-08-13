package com.jz.logger.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Setter
@NoArgsConstructor
public class TraceInfo {

    private String fieldName;

    private String tag;

    private int order;

    private Object oldValue;

    private Object newValue;

}