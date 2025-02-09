package com.yuwang.shorturlserver.adapter.cmd;

import lombok.Data;

@Data
public class ShortUrlQueryCmd {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String shortCode;
    private String longUrl;
}