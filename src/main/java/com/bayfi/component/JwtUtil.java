package com.bayfi.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public JwtUtil(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String generateJwt(Authentication auth, String userEmail) {
        log.info("Generating JWT for user: {}", userEmail);

        String scope = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        if (scope.isEmpty()) {
            scope = "ROLE_USER"; // Default role if no authorities are found
        }

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("unclebayo")
                .issuedAt(Instant.now())
                .subject(userEmail)
                .claim("roles", scope)
                .expiresAt(Instant.now().plusMillis(expiration))
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
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}