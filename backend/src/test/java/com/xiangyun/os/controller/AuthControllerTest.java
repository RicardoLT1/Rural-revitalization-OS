package com.xiangyun.os.controller;

import com.xiangyun.os.common.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest {

    @Test
    void loginReturnsDemoTokenAndUserInfo() {
        AuthController controller = new AuthController();

        ApiResponse<Map<String, Object>> response = controller.login(new AuthController.LoginRequest("admin", "123456"));

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData().get("token").toString()).startsWith("demo-");
        assertThat(response.getData()).containsKeys("tokenType", "expiresIn", "user");
    }

    @Test
    void meMarksBearerTokenAsAuthenticated() {
        AuthController controller = new AuthController();

        ApiResponse<Map<String, Object>> response = controller.me("Bearer demo-token");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData().get("authenticated")).isEqualTo(true);
    }
}
