package com.bayfi.config;

import com.bayfi.component.oauth2LoginSuccessHandler;
import com.bayfi.service.implementation.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final oauth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, com.bayfi.component.oauth2LoginSuccessHandler oauth2LoginSuccessHandler, JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.customUserDetailsService = customUserDetailsService;
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth-> auth
                        .requestMatchers("/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/v2/api-docs",
                                "/swagger-ui.html").permitAll() // public endpoint

                        .requestMatchers("api/v1/admin/**").hasRole("ADMIN") // admin-only endpoint
                        .anyRequest().authenticated()

                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .oauth2Login(oauth2 ->oauth2
                        .successHandler(oauth2LoginSuccessHandler)//Handle Oauth2 login success
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt-> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );


        return http.build();


    }


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtConverter;
    }




    }
