package com.jz.logger.core.util;

import cn.hutool.core.collection.CollUtil;
import com.jz.logger.core.matcher.Matcher;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Iterator;

/**
 * @Author JZ
 * @Date 2021/8/12 15:34
 */
@UtilityClass
public class CollectionUtils {

    public Object findFirst(Collection collection, Object target, Matcher matcher) {
        if (CollUtil.isEmpty(collection)) {
            return null;
        }
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (matcher.matches(element, target)) {
                return element;
            }
        }
        return null;
    }

}
