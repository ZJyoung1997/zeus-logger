package com.jz.logger.demo;

import com.jz.logger.core.handler.LoggerTraceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author JZ
 * @Date 2021/7/8 14:30
 */
@Configuration
public class CustomConfiguration {

//    @Bean("defaultLoggerTraceHandler")
    public LoggerTraceHandler loggerTraceHandler() {
        return new CustomTraceHandler();
    }

}
