package com.yuwang.shorturlserver.domain.dto;

import lombok.Data;

@Data
public class UrlCacheDTO {
    private String longUrl;
    private Long id;
}