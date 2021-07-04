package com.jz.logger.core.event;

import com.jz.logger.core.LoggerInfo;
import com.jz.logger.core.handler.LoggerTraceHandler;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoggerConsumEvent {

    private LoggerInfo loggerInfo;

    private LoggerTraceHandler loggerTraceHandler;

}
