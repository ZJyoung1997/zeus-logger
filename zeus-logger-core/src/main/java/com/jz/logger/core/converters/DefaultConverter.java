package com.jz.logger.core.converters;

/**
 * @Author JZ
 * @Date 2021/7/30 17:23
 */
public class DefaultConverter implements Converter<Object, Object> {

    @Override
    public Object transfor(Object o) {
        return o;
    }

}
