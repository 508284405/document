package com.yuwang.shorturlserver.adapter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwang.shorturlserver.adapter.cmd.ShortUrlCmd;
import com.yuwang.shorturlserver.adapter.cmd.ShortUrlQueryCmd;
import com.yuwang.shorturlserver.adapter.vo.BaseResult;
import com.yuwang.shorturlserver.adapter.vo.PageResult;
import com.yuwang.shorturlserver.adapter.vo.ShortUrlVO;
import com.yuwang.shorturlserver.config.ShortUrlProperties;
import com.yuwang.shorturlserver.domain.model.UrlEntity;
import com.yuwang.shorturlserver.domain.service.ShortUrlService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * REST controller exposing endpoints for creating and retrieving short URLs.
 * This controller delegates requests to the domain service and abides by the DDD architecture.
 */
@RestController
@RequestMapping("/api/shorturls")
@AllArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;
    private final ShortUrlProperties shortUrlProperties;


    // 1) Create a new short URL
    @PostMapping("/shorten")
    public BaseResult<String> createShortUrl(@RequestBody @Validated ShortUrlCmd cmd) {
        // request contains longUrl, custom alias (optional), expiration data, etc.
        String shortCode = shortUrlService.createShortUrl(cmd);
        String shortUrl = shortUrlProperties.getDomainPrefix() + "/" + shortCode;
        return BaseResult.success(shortUrl);
    }

    // 2) Redirect to the long URL
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortCode, HttpServletResponse httpResponse) {
        String longUrl = shortUrlService.getLongUrl(shortCode);
        if (longUrl == null) {
            // handle error: short code not found or expired
            return ResponseEntity.notFound().build();
        }
        // Perform redirection
        httpResponse.setHeader("Location", longUrl);
        httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).build();
    }

    // 3) Query short URL list with pagination
    @PostMapping("/list")
    public PageResult<List<ShortUrlVO>> listShortUrls(@RequestBody @Validated ShortUrlQueryCmd queryCmd) {
        Page<UrlEntity> page = new Page<>(queryCmd.getPageNum(), queryCmd.getPageSize());
        LambdaQueryWrapper<UrlEntity> queryWrapper = Wrappers.lambdaQuery(UrlEntity.class);

        if (StringUtils.hasText(queryCmd.getShortCode())) {
            queryWrapper.like(UrlEntity::getShortCode, queryCmd.getShortCode());
        }
        if (StringUtils.hasText(queryCmd.getLongUrl())) {
            queryWrapper.like(UrlEntity::getLongUrl, queryCmd.getLongUrl());
        }

        queryWrapper.orderByDesc(UrlEntity::getCreateTime);
        IPage<UrlEntity> pageResult = shortUrlService.page(page, queryWrapper);

        List<ShortUrlVO> voList = pageResult.getRecords().stream()
                .map(entity -> {
                    ShortUrlVO vo = new ShortUrlVO();
                    vo.setShortCode(entity.getShortCode());
                    vo.setLongUrl(entity.getLongUrl());
                    vo.setExpiresAt(entity.getExpiresAt());
                    vo.setClickCount(entity.getClickCount());
                    vo.setCreateTime(entity.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());

        return PageResult.success(voList, pageResult.getTotal(), pageResult.getSize(), pageResult.getCurrent());
    }
}
