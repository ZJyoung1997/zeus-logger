package com.jz.logger.core.converters;

public class DefaultMethodParameterConverter implements MethodParameterConverter<Object[]> {

    @Override
    public Object[] transform(Object[] objects) {
        return objects;
    }

}
