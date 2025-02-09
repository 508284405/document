package com.yuwang.shorturlserver.adapter.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShortUrlVO {
    private String shortCode;
    private String longUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long clickCount;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}