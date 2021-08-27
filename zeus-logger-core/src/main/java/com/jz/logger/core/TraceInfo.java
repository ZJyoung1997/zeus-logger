package com.jz.logger.core;

import lombok.*;

@Getter
@ToString
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TraceInfo {

    private String fieldName;

    private String tag;

    private int order;

    private Object oldValue;

    private Object newValue;

}