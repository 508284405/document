CREATE TABLE `urls` (
                        `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                        `short_code` VARCHAR(50) NOT NULL,
                        `long_url` TEXT NOT NULL,
                        `created_at` DATETIME NOT NULL,
                        `expires_at` DATETIME NULL,
                        `click_count` BIGINT NOT NULL DEFAULT 0,
                        `create_time` DATETIME NOT NULL,
                        `create_by` VARCHAR(50),
                        `update_time` DATETIME NOT NULL,
                        `update_by` VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `url_analytics` (
                                 `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 `short_code` VARCHAR(50) NOT NULL,
                                 `click_time` DATETIME NOT NULL,
                                 `user_ip` VARCHAR(45) NOT NULL,
                                 `user_agent` VARCHAR(255) NOT NULL,
                                 `geo_location` VARCHAR(100) NULL,
                                 `create_time` DATETIME NOT NULL,
                                 `create_by` VARCHAR(50),
                                 `update_time` DATETIME NOT NULL,
                                 `update_by` VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;