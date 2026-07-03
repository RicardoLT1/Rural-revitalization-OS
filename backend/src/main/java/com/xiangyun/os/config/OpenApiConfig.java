package com.xiangyun.os.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI xiangyunOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("乡耘OS 后端接口")
                        .description("用于小程序第一轮前后端联调的核心读接口")
                        .version("0.1.0"));
    }
}
