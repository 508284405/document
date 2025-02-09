This document extends the existing Short URL System Design to include detailed implementation specifics using **Spring Boot 3.3.8**, **MySQL 8**, **Redis 7** (accessed via **Redisson**), and **MyBatis-Plus** for data access.

---

#### 1. Introduction
The backend service in this Short URL System is responsible for:

1. Accepting long URLs and returning the corresponding shortened links.
2. Managing redirects from short URLs to the long URLs.
3. Storing and retrieving URL data from MySQL.
4. Caching frequently accessed links in Redis to improve performance.
5. Tracking analytics (if required) and managing link expiration.

With **Spring Boot 3.3.8**, we leverage modern Java features and simplify project configuration. **MyBatis-Plus** handles database CRUD operations. **Redis** improves performance by caching frequently used mappings, while **Redisson** provides thread-safe operations, distributed locking, and advanced data structures.

---

#### 2. Overall Architecture
1. **Controller Layer**: Exposes REST endpoints for URL creation and redirection.
2. **Service Layer**: Encapsulates business logic, including short URL generation, caching logic, and database interactions.
3. **Persistence Layer (DAO)**: Uses MyBatis-Plus to simplify CRUD operations on MySQL.
4. **Caching Layer**: Uses Redis (via Redisson) to cache URL mappings and store ephemeral data.

When a user submits a URL:

1. **Controller** receives the request and delegates it to the **Service**.
2. **Service** checks Redis cache to see if a short code is already mapped.
    - If absent, it queries the database, generates a new code if needed, and persists it.
    - The result (short code to long URL) is cached in Redis.
3. The **Controller** returns the short code in the response.
4. For redirection, if a user hits the endpoint with a short code, the system first checks Redis; if missed, it fetches from MySQL and updates Redis.

---

#### 3. Detailed Components
##### 3.1 Controller (Spring Boot)
+ **UrlController**: Provides endpoints for creating short links and handling redirects.

```java
@RestController
@RequestMapping("/api/url")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    // 1) Create a new short URL
    @PostMapping("/shorten")
    public ResponseEntity<ShortUrlResponse> createShortUrl(@RequestBody ShortUrlRequest request) {
        // request contains longUrl, custom alias (optional), expiration data, etc.
        String shortCode = urlService.createShortUrl(request);
        String shortUrl = "https://short.ly/" + shortCode;
        ShortUrlResponse response = new ShortUrlResponse(shortUrl);
        return ResponseEntity.ok(response);
    }

    // 2) Redirect to the long URL
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortCode, HttpServletResponse httpResponse) {
        String longUrl = urlService.getLongUrl(shortCode);
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
```

+ **Endpoints**:
    - **POST** `/api/url/shorten`: Takes a `ShortUrlRequest` JSON body containing `longUrl`, optional `customAlias`, `expirationDate`, etc.
    - **GET** `/api/url/{shortCode}`: Redirects to the associated `longUrl`.

##### 3.2 Service Layer
+ **UrlService**: Contains the logic for generating short codes, storing them in MySQL, caching in Redis, and retrieving them.

```java
@Service
public class UrlService {

    private final UrlMapper urlMapper; // MyBatis-Plus mapper
    private final RedissonClient redissonClient; // Redisson client
    private final StringRedisTemplate redisTemplate; // or we could use Redisson structures

    public UrlService(UrlMapper urlMapper, RedissonClient redissonClient, StringRedisTemplate redisTemplate) {
        this.urlMapper = urlMapper;
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
    }

    public String createShortUrl(ShortUrlRequest request) {
        // 1) Validate the request
        // 2) Generate or validate custom alias
        String shortCode = (request.getCustomAlias() != null && !request.getCustomAlias().isEmpty())
                ? validateCustomAlias(request.getCustomAlias())
                : generateShortCode(request.getLongUrl());

        // 3) Check if code exists in DB or Redis
        String cacheKey = "url:" + shortCode;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey)) || urlMapper.selectById(shortCode) != null) {
            // if shortCode is not unique, handle collision or throw exception
        }

        // 4) Persist to DB
        UrlEntity entity = new UrlEntity();
        entity.setShortCode(shortCode);
        entity.setLongUrl(request.getLongUrl());
        entity.setExpiresAt(request.getExpiresAt());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setClickCount(0L);
        urlMapper.insert(entity);

        // 5) Cache in Redis
        redisTemplate.opsForValue().set(cacheKey, request.getLongUrl());
        if (request.getExpiresAt() != null) {
            long ttl = Duration.between(LocalDateTime.now(), request.getExpiresAt()).toSeconds();
            redisTemplate.expire(cacheKey, Duration.ofSeconds(ttl));
        }

        return shortCode;
    }

    public String getLongUrl(String shortCode) {
        String cacheKey = "url:" + shortCode;
        // 1) Check Redis cache first
        String longUrl = redisTemplate.opsForValue().get(cacheKey);
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
    return sb.length() > 0 ? sb.toString() : "0";
}


    private String validateCustomAlias(String customAlias) {
        // check for valid characters, length, etc.
        // if invalid, throw an exception
        return customAlias;
    }
}
```

##### 3.3 Repository/DAO Layer (MyBatis-Plus)
**UrlMapper** interface extends MyBatis-Plus base mapper and provides standard CRUD operations. The `shortCode` is used as the primary key.

```java
@Mapper
public interface UrlMapper extends BaseMapper<UrlEntity> {
}
```

##### 3.4 Entity Classes
**UrlEntity**: Maps to the **URLs Table** in MySQL.

```java
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
```

+ We set `shortCode` as the **primary key** (`@TableId`) with `IdType.INPUT` since we generate it ourselves.

##### 3.5 Redis Caching with Redisson
We use **Redisson** for advanced Redis features (locks, pub/sub, etc.). For simple key-value operations, we can also use **StringRedisTemplate**.

**Configuration**:

```java
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://" + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }
}
```

**Usage**:

+ **Locking**: In `incrementClickCount`, we acquire a distributed lock to ensure concurrency safety across multiple nodes.
+ **Caching**: We store and retrieve the `longUrl` in Redis with a key pattern `url:{shortCode}`.

---

#### 4. Database Schema (MySQL 8)
A possible DDL for the `` table:

```sql
CREATE TABLE `urls` (
    `short_code` VARCHAR(50) NOT NULL,
    `long_url` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `expires_at` DATETIME NULL,
    `click_count` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`short_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

If you choose to maintain analytics in a separate table:

```sql
CREATE TABLE `url_analytics` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `short_code` VARCHAR(50) NOT NULL,
    `click_time` DATETIME NOT NULL,
    `user_ip` VARCHAR(45) NOT NULL,
    `user_agent` VARCHAR(255) NOT NULL,
    `geo_location` VARCHAR(100) NULL,
    INDEX idx_short_code (`short_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

#### 5. Configuration & Properties
**application.yml** snippet:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shorturl_db?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: password
  redis:
    host: localhost
    port: 6379

mybatis-plus:
  mapper-locations: classpath*:/mappers/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

Add dependencies in `pom.xml`:

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>

        <artifactId>spring-boot-starter-web</artifactId>

        <version>3.3.8</version>

    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>

        <artifactId>mybatis-plus-boot-starter</artifactId>

        <version>3.5.3.1</version>

    </dependency>

    <!-- MySQL Driver -->
    <dependency>
        <groupId>mysql</groupId>

        <artifactId>mysql-connector-java</artifactId>

        <scope>runtime</scope>

    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>

        <artifactId>spring-boot-starter-data-redis</artifactId>

    </dependency>

    <!-- Redisson -->
    <dependency>
        <groupId>org.redisson</groupId>

        <artifactId>redisson-spring-boot-starter</artifactId>

        <version>3.20.0</version>

    </dependency>

    <!-- Commons-Lang for random code generation -->
    <dependency>
        <groupId>org.apache.commons</groupId>

        <artifactId>commons-lang3</artifactId>

        <version>3.12.0</version>

    </dependency>

</dependencies>

```

---

#### 6. Additional Considerations
1. **Expiry Handling**: The system checks if `expires_at` is in the past whenever retrieving from the DB/Redis. If expired, the link is considered invalid. You can also schedule a cleanup job to remove expired records.
2. **Rate Limiting & Security**: Implement a rate-limiting filter or use a gateway (e.g., Spring Cloud Gateway) for advanced security.
3. **Analytics**: Extend the solution to capture request metadata and store it in a separate `url_analytics` table.
4. **Error Handling**: For collisions or invalid aliases, throw custom exceptions and handle them with Spring’s `@ControllerAdvice`.
5. **Deployment**: Deploy on a multi-node environment with a load balancer. Use a shared Redis cluster for caching and MySQL cluster for persistent storage.

---

#### 7. Conclusion
This backend service architecture design leverages **Spring Boot 3.3.8**, **MySQL 8**, **Redis 7** (via **Redisson**), and **MyBatis-Plus** to create a scalable, performant, and maintainable short URL system. The outlined controllers, services, and data access patterns ensure a clean separation of concerns and enable easy scaling. With Redis caching, we significantly reduce database load and enhance performance for frequently accessed short links. Distributed locking via Redisson ensures data consistency in high-concurrency environments.

