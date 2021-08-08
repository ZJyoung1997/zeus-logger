package com.jz.logger.demo.conveter;

import com.jz.logger.core.converters.MethodParameterConverter;
import com.jz.logger.demo.pojo.TestData;

public class AllArgsConverter implements MethodParameterConverter<Object[], Object[]> {
    @Override
    public Object[] transform(Object[] objects) {
        TestData data = (TestData) objects[0];
        Object[] result = new Object[2];
        result[0] = data.getFamily().getId();
        return result;
    }
}
