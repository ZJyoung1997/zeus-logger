package com.jz.logger.demo.controller;

import com.jz.logger.core.annotation.Logger;
import com.jz.logger.core.enumerate.Strategy;
import com.jz.logger.demo.conveter.AllArgsConverter;
import com.jz.logger.demo.pojo.TestData;

public interface BaseController extends BaseInterface {

    @Logger(selectParam = "family?.id", selectMethod = "@mapper.get(#root[0])",
            topic = "更新房主和朋友圈", methodParamConverter = AllArgsConverter.class)
    Object update(TestData data);

}
