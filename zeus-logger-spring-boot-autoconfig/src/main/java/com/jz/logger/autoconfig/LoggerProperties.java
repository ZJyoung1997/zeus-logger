package com.jz.logger.autoconfig;

import com.jz.logger.core.LoggerExtensionData;
import com.jz.logger.core.enumerate.Strategy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "zeus.logger")
public class LoggerProperties {

    /**
     * 默认处理策略，默认异步顺序执行
     */
    private Strategy defaultStrategy = Strategy.ASYN_SERIAL;

    /**
     * 默认失败重试次数，默认3，负数为不重试
     */
    private int defaultRetryTimes = 3;

    /**
     * 实现了 {@link LoggerExtensionData} 接口的类的全限定名
     */
    private List<String> globalExtensionDatas;

    private Disruptor disruptor = new Disruptor();

    @Setter
    @Getter
    public static class Disruptor {

        private SerialConfig serial = new SerialConfig();

        private ConcurrentConfig concurrent = new ConcurrentConfig();

    }

    @Setter
    @Getter
    public static class SerialConfig extends BaseDisruptorConfig {

        /**
         * 线程名称前缀
         */
        private String threadNamePrefix = "zeus-logger-serial-";

    }

    @Setter
    @Getter
    public static class ConcurrentConfig extends BaseDisruptorConfig {

        /**
         * 线程名称前缀
         */
        private String threadNamePrefix = "zeus-logger-concurrent";

        /**
         * 并发模式 disruptor 消费者数量
         */
        private int concurrentCustomerNum = 4;

        public int getConcurrentCustomerNum() {
            return concurrentCustomerNum <= 0 ?
                    Runtime.getRuntime().availableProcessors() :
                    concurrentCustomerNum;
        }

    }

    @Setter
    @Getter
    public static class BaseDisruptorConfig {

        private static final int RING_BUFFER_MIN = 16;

        /**
         * 是否开启该模式
         */
        private boolean enabled = true;

        /**
         * disruptor 队列长度
         */
        private int ringBufferSize = RING_BUFFER_MIN;

        /**
         * disruptor 消费者等待策略
         */
        private WaitStrategy waitStrategy = WaitStrategy.SLEEPING;

        private int sleepingWaitStrategyRetries = 20;

        /**
         * 单位 纳秒，默认 10 毫秒
         */
        private long sleepingWaitStrategySleepTime = 1000 * 1000 * 10;

        public int getRingBufferSize() {
            return Math.max(ringBufferSize, RING_BUFFER_MIN);
        }

    }

}
