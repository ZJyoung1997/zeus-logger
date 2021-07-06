package com.jz.logger.autoconfig;

import com.jz.logger.core.LoggerExtensionData;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "zeus.logger")
public class LoggerProperties {

    private static final int DEFAULT_RING_BUFFER_SIZE = 1024;

    /**
     * 日志并发处理数量
     */
    private Integer concurrentNum;

    private int concurrentRingBufferSize = DEFAULT_RING_BUFFER_SIZE;

    private int serialRingBufferSize = DEFAULT_RING_BUFFER_SIZE;

    /**
     * 实现了 {@link LoggerExtensionData} 接口的类的全限定名
     */
    private List<String> globalExtensionDatas;

}
