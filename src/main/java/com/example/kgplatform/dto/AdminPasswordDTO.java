package com.example.kgplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminPasswordDTO {
    @NotNull(message = "目标用户ID不能为空")
    private Long targetUserId;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
