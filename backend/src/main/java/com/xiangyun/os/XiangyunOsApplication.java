package com.xiangyun.os;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xiangyun.os.mapper")
public class XiangyunOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiangyunOsApplication.class, args);
    }
}
