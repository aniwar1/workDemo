package com.example.kgplatform.dto;

import lombok.Data;

@Data
public class LoginVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String token;
    private Long roleId;
    private String roleName;
    private String roleCode;
}
