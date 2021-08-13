package com.jz.logger.core.matcher;

/**
 * 匹配器，用于判断两个对象是否匹配
 * @Author JZ
 * @Date 2021/8/12 15:19
 */
public interface Matcher<O> {

    boolean matches(O o1, O o2);

}
