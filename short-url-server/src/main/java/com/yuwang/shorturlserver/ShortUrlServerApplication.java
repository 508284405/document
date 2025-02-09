package com.yuwang.shorturlserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yuwang.shorturlserver.domain.repository")
public class ShortUrlServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortUrlServerApplication.class, args);
    }

}
