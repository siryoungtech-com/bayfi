package com.bayfi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Data
public class SignInRequest {

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    @NotNull(message = "email field is required")
    @NotBlank(message = "email field cannot be blank")
    @Email(message = "invalid email format")
    private String email;

    @Schema(description = "Password of the user", example = "Password123!", type = "string")
    @NotNull(message = "password field is required")
    @NotBlank(message = "password field cannot be blank")
    private String password;
}
