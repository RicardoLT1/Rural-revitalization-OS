package com.xiangyun.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessErrorUsesMatchingHttpStatus() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusiness(
                new BusinessException(40300, "无权操作"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(40300);
    }

    @Test
    void validationAndUnexpectedErrorsUseStandardHttpStatuses() {
        assertThat(handler.handleValidation(new IllegalArgumentException()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(handler.handleException(new IllegalStateException()).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
