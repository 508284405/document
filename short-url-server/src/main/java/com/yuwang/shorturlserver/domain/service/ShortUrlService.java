package com.yuwang.shorturlserver.domain.service;

import com.yuwang.shorturlserver.adapter.cmd.ShortUrlCmd;
import com.yuwang.shorturlserver.adapter.exception.BusinessException;
import com.yuwang.shorturlserver.domain.model.UrlEntity;
import com.yuwang.shorturlserver.domain.repository.ShortUrlMapper;
import lombok.AllArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ShortUrlService {
    // MyBatis-Plus mapper
    private final ShortUrlMapper urlMapper;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    public String createShortUrl(ShortUrlCmd request) {
        // 1) Validate the request
        // 2) Generate or validate custom alias
        String originalUrl = request.getOriginalUrl();
        String shortCode = (request.getShortCode() != null && !request.getShortCode().isEmpty())
                ? validateCustomAlias(request.getShortCode())
                : generateShortCode(originalUrl);

        // 3) Check if code exists in DB or Redis
        String cacheKey = "url:" + shortCode;
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        if (bucket.isExists() || urlMapper.selectById(shortCode) != null) {
            // if shortCode is not unique, handle collision or throw exception
            throw new BusinessException("Short code is not unique");
        }

        // 4) Persist to DB
        UrlEntity entity = new UrlEntity();
        entity.setShortCode(shortCode);
        entity.setLongUrl(originalUrl);
        entity.setExpiresAt(request.getExpiresAt());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setClickCount(0L);
        urlMapper.insert(entity);

        // 5) Cache in Redis
        redisTemplate.opsForValue().set(cacheKey, originalUrl);
        if (request.getExpiresAt() != null) {
            long ttl = Duration.between(LocalDateTime.now(), request.getExpiresAt()).toSeconds();
            redisTemplate.expire(cacheKey, Duration.ofSeconds(ttl));
        }

        return shortCode;
    }

    public String getLongUrl(String shortCode) {
        String cacheKey = "url:" + shortCode;
        // 1) Check Redis cache first
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        String longUrl = bucket.get();
        if (longUrl != null) {
            // increment click count in background
            incrementClickCount(shortCode);
            return longUrl;
        }

        // 2) If not found in cache, query DB
        UrlEntity entity = urlMapper.selectById(shortCode);
        if (entity == null) {
            return null; // short code not found
        }

        // 3) Check expiration
        if (entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            // URL is expired
            return null;
        }

        longUrl = entity.getLongUrl();
        // 4) Update cache
        redisTemplate.opsForValue().set(cacheKey, longUrl);
        if (entity.getExpiresAt() != null) {
            long ttl = Duration.between(LocalDateTime.now(), entity.getExpiresAt()).toSeconds();
            redisTemplate.expire(cacheKey, Duration.ofSeconds(ttl));
        }

        incrementClickCount(shortCode);
        return longUrl;
    }

    private void incrementClickCount(String shortCode) {
        // increments the click count in DB and can update the cache
        // using Redisson locks for concurrency if needed
        RLock lock = redissonClient.getLock("lock:count:" + shortCode);
        try {
            lock.lock();
            UrlEntity entity = urlMapper.selectById(shortCode);
            if (entity != null) {
                entity.setClickCount(entity.getClickCount() + 1);
                urlMapper.updateById(entity);
            }
        } finally {
            lock.unlock();
        }
    }

    private String generateShortCode(String longUrl) {
    // 1) Hash the long URL using SHA-256 and convert to Base62
    String hashValue = generateHash(longUrl);

    // 2) Take the first 7 characters from the hash value for the short code
    String shortCode = hashValue.substring(0, 7);

    // 3) Use Redisson Bloom Filter to check for hash conflicts
    RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("urlBloomFilter");

    // If the short code already exists in the Bloom Filter, it means we have a conflict
    if (bloomFilter.contains(shortCode)) {
        // Conflict detected, apply hash+ logic by adding salt to avoid collision
        String newHashValue = generateHash(longUrl + System.nanoTime()); // Adding salt
        shortCode = newHashValue.substring(0, 7);  // Take the first 7 characters of the new hash
    }

    // 4) Add the new shortCode to the Bloom Filter to prevent future conflicts
    bloomFilter.add(shortCode);

    return shortCode;
}

// Helper method to generate SHA-256 hash and convert it to Base62
private String generateHash(String input) {
    try {
        // SHA-256 hash generation
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        
        // Convert the hash bytes to Base62
        return base62Encode(hashBytes);
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("Hashing algorithm error", e);
    }
}

// Helper method to encode bytes in Base62
private String base62Encode(byte[] input) {
    final String base62Chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    BigInteger bigInt = new BigInteger(1, input);
    StringBuilder sb = new StringBuilder();

    while (bigInt.compareTo(BigInteger.ZERO) > 0) {
        BigInteger[] divRem = bigInt.divideAndRemainder(BigInteger.valueOf(62));
        bigInt = divRem[0];
        int remainder = divRem[1].intValue();
        sb.insert(0, base62Chars.charAt(remainder));
    }

    // If the hash was 0, represent it as '0'
    return !sb.isEmpty() ? sb.toString() : "0";
}


    private String validateCustomAlias(String customAlias) {
        // check for valid characters, length, etc.
        // if invalid, throw an exception
        return customAlias;
    }
}