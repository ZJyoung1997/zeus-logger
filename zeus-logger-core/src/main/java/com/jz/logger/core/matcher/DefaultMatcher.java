package com.jz.logger.core.matcher;

import java.util.Objects;

/**
 * @Author JZ
 * @Date 2021/8/12 15:25
 */
public class DefaultMatcher implements Matcher<Object> {

    @Override
    public boolean matches(Object a, Object b) {
        return Objects.equals(a, b);
    }

}
