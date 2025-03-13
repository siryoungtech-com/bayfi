package com.bayfi.dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Data
public class SignUpRequest {
    @NotNull(message = "firstname field is required")
    @NotBlank(message = "firstname field cannot be blank")
    private String firstname;

    @NotNull(message = "lastname field is required")
    @NotBlank(message = "Lastname field cannot be blank")
    private String lastname;

    @NotNull(message = "email field is required")
    @NotBlank(message = "email field cannot be blank")
    @Email(message = "invalid email format")
    private String email;

    @NotNull(message = "username filed is required")
    @NotBlank(message = "The username field cannot be blank")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    @NotNull(message = "password field is required")
    @NotBlank(message = "password field cannot be blank")
    private String password;


}
