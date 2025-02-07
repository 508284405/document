package com.yuwang.shorturlserver.adapter.controller;

import com.yuwang.shorturlserver.adapter.cmd.ShortUrlCmd;
import com.yuwang.shorturlserver.adapter.vo.BaseResult;
import com.yuwang.shorturlserver.domain.service.ShortUrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
// Optionally, you might use RedirectView to perform redirections
// import org.springframework.web.servlet.view.RedirectView;


/**
 * REST controller exposing endpoints for creating and retrieving short URLs.
 * This controller delegates requests to the domain service and abides by the DDD architecture.
 */
@RestController
@RequestMapping("/api/shorturls")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    @Autowired
    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    // 1) Create a new short URL
    @PostMapping("/shorten")
    public BaseResult<String> createShortUrl(@RequestBody @Validated ShortUrlCmd cmd) {
        // request contains longUrl, custom alias (optional), expiration data, etc.
        String shortCode = shortUrlService.createShortUrl(cmd);
        String shortUrl = "https://short.ly/" + shortCode;
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
}
