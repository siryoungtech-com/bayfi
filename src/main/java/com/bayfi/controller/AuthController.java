package com.bayfi.controller;

import com.bayfi.dto.Request.SignInRequest;
import com.bayfi.dto.Request.SignUpRequest;
import com.bayfi.service.implementation.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Auth APIs")
@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user", description = "Register a new user with the provided details")
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> registerUser(@RequestBody @Valid SignUpRequest request){
        return authService.registerUser(request);
    }



    @Operation(summary = "Authenticate a user", description = "The jwt provided after successful authentication will be used to access other end points by the user")
    @PostMapping("/sign-in")
    public ResponseEntity<String> authenticateUser(@RequestBody @Valid SignInRequest signInRequest){
        return authService.authenticateUser(signInRequest);
    }


}

