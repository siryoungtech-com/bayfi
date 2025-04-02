package com.bayfi.service;

import com.bayfi.dto.request.SignInRequest;
import com.bayfi.dto.request.SignUpRequest;

import java.net.URI;

public interface AuthService {
    URI registerUser(SignUpRequest request);

    String authenticateUser(SignInRequest signInRequest);

}
