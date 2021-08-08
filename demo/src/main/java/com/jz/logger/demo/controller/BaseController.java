package com.jz.logger.demo.controller;

import com.jz.logger.core.annotation.Logger;
import com.jz.logger.demo.pojo.TestData;

public interface BaseController extends BaseInterface {

    @Logger(selectParam = "family?.id", selectMethod = "@mapper.get(#root)", topic = "更新房主和朋友圈")
    Object update(TestData data);

}
