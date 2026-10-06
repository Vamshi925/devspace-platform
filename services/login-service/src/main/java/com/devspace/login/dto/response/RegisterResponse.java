package com.devspace.login.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {

    private String userId;

    private String name;

    private String email;

    private String phoneNumber;

    private String role;
}