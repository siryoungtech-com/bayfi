package com.bayfi.service;

import org.springframework.stereotype.Service;

@Service
public interface UserService {

    void processOauth2User(String email, String provider, String providerId);
}
