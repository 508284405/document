package com.yuwang.shorturlserver.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@TableName("urls")
public class UrlEntity extends BaseEntity{
    private String shortCode; // as the primary key

    private String longUrl;

    private LocalDateTime expiresAt;

    private Long clickCount;
}