package com.yuwang.shorturlserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "short-url")
public class ShortUrlProperties {
    /**
     * 短链接域名前缀，默认为 https://short.ly
     */
    private String domainPrefix = "http://localhost:8080/";
}