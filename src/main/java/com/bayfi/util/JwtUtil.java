package com.bayfi.util;

import com.bayfi.constant.JwtConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtil {


    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;


    public JwtUtil(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String generateJwt(Authentication auth, String userEmail) {
        log.info("Generating JWT for user: {}", userEmail);

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toList());


        if (roles.isEmpty()) {
            roles.add("ROLE_USER"); // Default role if no authorities are found
        }

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("bayfi")
                .issuedAt(Instant.now())
                .subject(userEmail)
                .claim("roles", roles)
                .expiresAt(Instant.now().plusMillis(JwtConstant.JWT_EXPIRATION_TIME))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt decodeJwt(String token) {
        log.info("Decoding JWT token");
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            log.error("Failed to decode JWT token", e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public Collection<? extends GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extract roles from the JWT and convert them to GrantedAuthority objects
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}