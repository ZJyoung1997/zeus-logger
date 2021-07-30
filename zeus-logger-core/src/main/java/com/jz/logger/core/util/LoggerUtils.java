package com.jz.logger.core.util;

import cn.hutool.core.util.ArrayUtil;
import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.annotation.Trace;
import lombok.experimental.UtilityClass;

/**
 * @Author JZ
 * @Date 2021/7/30 16:31
 */
@UtilityClass
public class LoggerUtils {

    public boolean isMatch(Logger logger, Trace trace) {
        if (trace.topic().length > 0 && !ArrayUtil.contains(trace.topic(), logger.topic())) {
            return false;
        }
        if (trace.resourceType().length > 0 && !ArrayUtil.contains(trace.resourceType(), logger.resourceType())) {
            return false;
        }
        if (trace.operationType().length > 0 && !ArrayUtil.contains(trace.operationType(), logger.operationType())) {
            return false;
        }
        return true;
    }

}
