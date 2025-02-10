package com.yuwang.shorturlserver.adapter.controller;

import com.yuwang.shorturlserver.domain.model.UrlAccessLog;
import com.yuwang.shorturlserver.domain.repository.UrlAccessLogMapper;
import com.yuwang.shorturlserver.domain.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;

/**
 * Controller responsible for handling URL redirections.
 * This controller follows the Single Responsibility Principle by focusing solely on redirect operations.
 */
@RestController
@AllArgsConstructor
public class RedirectController {

    private final ShortUrlService shortUrlService;
    private final UrlAccessLogMapper urlAccessLogMapper;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable("shortCode") String shortCode, 
            HttpServletRequest request, HttpServletResponse response) {
        String longUrl = shortUrlService.getLongUrl(shortCode);
        if (longUrl == null) {
            // handle error: short code not found or expired
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // 记录访问日志
        UrlAccessLog accessLog = new UrlAccessLog();
        accessLog.setShortCode(shortCode);
        accessLog.setClickTime(LocalDateTime.now());
        accessLog.setUserIp(request.getRemoteAddr());
        accessLog.setUserAgent(request.getHeader("User-Agent"));
        // TODO: 可以通过IP地理位置服务获取位置信息
        accessLog.setGeoLocation("Unknown");
        urlAccessLogMapper.insert(accessLog);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(longUrl));
        // 返回的响应将包含 302 状态码和 Location 头
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}