package com.weymu.xlsx.common;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class RequestDTO {
    private String wbId;
    private String title;
    private List<JSONObject> datas;
}
