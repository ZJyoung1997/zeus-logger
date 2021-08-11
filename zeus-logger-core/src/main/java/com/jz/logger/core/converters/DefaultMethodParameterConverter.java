package com.jz.logger.core.converters;

public class DefaultMethodParameterConverter implements MethodParameterConverter<Object[]> {

    @Override
    public Object[] transfor(Object[] objects) {
        return objects;
    }

}
