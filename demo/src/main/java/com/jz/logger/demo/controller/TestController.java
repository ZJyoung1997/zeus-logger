package com.jz.logger.demo.controller;

import com.jz.logger.core.annotation.Logger;
import com.jz.logger.demo.mapper.Mapper;
import com.jz.logger.demo.pojo.TestData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final Mapper mapper;

    @Logger(selectParam = "family.id", selectMethod = "@mapper.get(#root)", topic = "更新房主和朋友圈")
    @PostMapping(value = "/update")
    public Object update(@RequestBody TestData data) {
        mapper.put(data.getFamily());
        return Mapper.cacheMap.get(data.getFamily().getId());
    }

    @Logger(selectParam = "familyList?.![id]", selectMethod = "@mapper.get(#root)", topic = "批量更新房子信息")
    @PostMapping(value = "/batch_update")
    public Object batchUpdate(@RequestBody TestData data) {
        if (data.getFamilyList() != null) {
            data.getFamilyList().forEach(family -> mapper.put(family));
            return data.getFamilyList().stream()
                    .map(family -> mapper.get(family.getId()))
                    .collect(Collectors.toList());
        }
        return null;
    }

}
