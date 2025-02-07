package com.yuwang.shorturlserver.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("urls")
public class UrlEntity {
    @TableId(type = IdType.INPUT)
    private String shortCode; // as the primary key

    private String longUrl;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("click_count")
    private Long clickCount;
}