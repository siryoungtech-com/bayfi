package com.bayfi.controller;

import com.bayfi.service.implementation.Oauth2LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Oauth2", description = "Oauth2 APIs")
@RestController
@RequestMapping("/api/v1/auth")
public class Oauth2Controller {

    private  final Oauth2LoginService oauth2LoginService;

    public Oauth2Controller(Oauth2LoginService oauth2LoginService) {
        this.oauth2LoginService = oauth2LoginService;
    }

    @Operation(summary = "Authenticate a user with Oauth2Login", description = "to be updated")
    @GetMapping("/authorize/{provider}")
    public ResponseEntity<String> initiateOauth2Login(@PathVariable String provider){
        String authorizationUrl = oauth2LoginService.getAuthorizationUrl(provider);
        return ResponseEntity.ok(authorizationUrl);
    }

    @GetMapping("oauth2/callback/{provider}")
    public ResponseEntity<String> exchangeCodeForJwt(@PathVariable String provider, @RequestParam String code) {
        String jwt = oauth2LoginService.exchangeCodeForJwt(provider, code);
        return ResponseEntity.ok(jwt);
    }

}
