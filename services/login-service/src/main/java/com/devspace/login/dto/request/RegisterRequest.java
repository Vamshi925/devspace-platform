package com.devspace.login.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(
            message = "Name is required"
    )
    @Size(
            min = 2,
            max = 100,
            message = "Name must contain between 2 and 100 characters"
    )
    private String name;

    @NotBlank(
            message = "Email is required"
    )
    @Email(
            message = "Enter a valid email address"
    )
    private String email;

    @NotBlank(
            message = "Phone number is required"
    )
    @Pattern(
            regexp = "^\\+?[1-9][0-9]{7,14}$",
            message = "Enter a valid phone number"
    )
    private String phoneNumber;

    @NotBlank(
            message = "Password is required"
    )
    @Size(
            min = 8,
            max = 100,
            message = "Password must contain at least 8 characters"
    )
    private String password;
}