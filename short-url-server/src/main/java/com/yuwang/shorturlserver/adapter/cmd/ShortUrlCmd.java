package com.yuwang.shorturlserver.adapter.cmd;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortUrlCmd {
    private String longUrl;
    private String shortCode;
    private LocalDateTime expiresAt;
}
