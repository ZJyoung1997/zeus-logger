package com.jz.logger.core;

import com.jz.logger.core.annotation.Trace;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldInfo {

    private Trace trace;

    private Field field;

    public FieldInfo(Trace trace, Field field) {
        this.trace = trace;
        this.field = field;
    }

}
