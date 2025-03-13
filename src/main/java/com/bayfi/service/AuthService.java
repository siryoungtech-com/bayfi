package com.bayfi.service;

import com.bayfi.dto.Request.SignUpRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<String> registerUser(SignUpRequest request);

    //SignInResponse loginUser(SignInRequest request);
}
