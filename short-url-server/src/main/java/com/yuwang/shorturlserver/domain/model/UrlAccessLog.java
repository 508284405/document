package com.yuwang.shorturlserver.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@TableName("url_analytics")
public class UrlAccessLog extends BaseEntity {
    private String shortCode;
    private LocalDateTime clickTime;
    private String userIp;
    private String userAgent;
    private String geoLocation;
}