package com.jz.logger.autoconfig;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;

public enum WaitStrategy {

    /**
     * {@link SleepingWaitStrategy}
     */
    SLEEPING,

    /**
     * {@link BlockingWaitStrategy}
     */
    BLOCKING,

    /**
     * {@link YieldingWaitStrategy}
     */
    YIELDING

    ;

}
