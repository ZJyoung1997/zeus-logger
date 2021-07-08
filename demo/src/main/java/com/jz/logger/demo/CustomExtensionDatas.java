package com.jz.logger.demo;

import com.jz.logger.core.LoggerExtensionData;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author JZ
 * @Date 2021/7/8 14:21
 */
@Component
public class CustomExtensionDatas implements LoggerExtensionData {

    @Override
    public Map<String, Object> getExtData() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", 1);
        return map;
    }
}
