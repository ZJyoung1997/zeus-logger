package com.jz.logger.core.converters;

/**
 * @Author JZ
 * @Date 2021/7/30 17:14
 */
public interface Converter<T, R> {

    /**
     * 将入参转换为出参
     * @param t   待转换对象
     * @return    转换后对象
     */
    R transfor(T t);

}
