package com.jz.logger.demo.pojo;

import com.jz.logger.core.annotation.Trace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Family {

    private Integer id;

    @Trace(tag = "家庭名称")
    private String name;

    @Trace(tag = "主人", targetValue = "#root?.name")
    private People host;

    @Trace(tag = "朋友圈", targetValue = "#root?.![name]")
    private List<People> friends;

}
