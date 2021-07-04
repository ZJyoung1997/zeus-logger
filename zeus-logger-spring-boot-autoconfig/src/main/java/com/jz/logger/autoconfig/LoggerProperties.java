package com.jz.logger.autoconfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

}
