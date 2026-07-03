package com.xiangyun.os.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一接口返回结构")
public class ApiResponse<T> {

    @Schema(description = "业务状态码，成功为 200")
    private Integer code;

    @Schema(description = "响应说明")
    private String message;

    @Schema(description = "业务数据")
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
