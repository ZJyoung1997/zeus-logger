package com.jz.logger.core;

import java.util.Map;

/**
 * 日志扩展数据
 * @Author JZ
 * @Date 2021/7/6 12:26
 */
public interface LoggerExtensionData {

    default Map<String, Object> getExtData() {
        return null;
    }

    /**
     * @param oldObject 方法执行前 {@link com.jz.logger.core.annotation.Logger#selectParam()} 结果
     * @param newObject 方法执行后 {@link com.jz.logger.core.annotation.Logger#selectParam()} 结果
     * @return
     */
    default Map<String, Object> getExtData(Object oldObject, Object newObject) {
        return null;
    }

}
