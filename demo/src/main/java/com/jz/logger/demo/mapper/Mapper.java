package com.jz.logger.demo.mapper;

import cn.hutool.core.collection.ListUtil;
import com.jz.logger.demo.pojo.Family;
import com.jz.logger.demo.pojo.People;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class Mapper {

    public static Map<Integer, Family> cacheMap = new HashMap<>();

    static {
        cacheMap.put(1, new Family(1, "张家", new People(1, "张1"), ListUtil.toList(new People(2, "张2"))));
        cacheMap.put(2, new Family(2, "王家", new People(3, "王1"), ListUtil.toList(new People(4, "王2"), new People(5, "王3"))));
    }

    public List<Family> get(List<Integer> ids) {
        return ids.stream().map(id -> cacheMap.get(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Family get(Integer id) {
        return cacheMap.get(id);
    }

    public void put(Family family) {
        cacheMap.put(family.getId(), family);
    }

}
