package com.jz.logger.core.enumerate;

public enum Strategy {

    DEFAULT,

    SYNC,

    /**
     * 异步串行执行
     * 对日志的处理将异步执行，且根据日志产生的先后顺序，顺序处理
     */
    ASYN_SERIAL,

    /**
     * 异步并发执行
     * 对日志的处理将异步执行，且不论日志产生的先后顺序如何，并发处理
     */
    ASYN_CONCURRENT

}
