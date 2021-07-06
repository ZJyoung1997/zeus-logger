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
     * @param param {@link com.jz.logger.core.annotation.Logger#selectParam()} 结果
     * @return
     */
    default Map<String, Object> getExtData(Object param) {
        return null;
    }

}
