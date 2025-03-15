package com.bayfi.service;

import com.bayfi.dto.Request.SignInRequest;
import com.bayfi.dto.Request.SignUpRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<String> registerUser(SignUpRequest request);

    ResponseEntity<String> authenticateUser(SignInRequest signInRequest);


    //SignInResponse loginUser(SignInRequest request);
}
