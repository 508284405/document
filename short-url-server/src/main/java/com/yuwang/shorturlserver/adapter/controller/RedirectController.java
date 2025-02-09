package com.yuwang.shorturlserver.adapter.controller;

import com.yuwang.shorturlserver.domain.service.ShortUrlService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controller responsible for handling URL redirections.
 * This controller follows the Single Responsibility Principle by focusing solely on redirect operations.
 */
@Controller
@AllArgsConstructor
public class RedirectController {

    private final ShortUrlService shortUrlService;

    @GetMapping("/{shortCode}")
    public void redirectToLongUrl(@PathVariable("shortCode") String shortCode, HttpServletResponse httpResponse) {
        String longUrl = shortUrlService.getLongUrl(shortCode);
        if (longUrl == null) {
            // handle error: short code not found or expired
            httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Perform redirection
        httpResponse.setHeader("Location", longUrl);
        httpResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
    }
}