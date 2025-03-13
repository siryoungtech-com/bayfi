package com.bayfi.service.implementation;

import com.bayfi.dto.Request.SignUpRequest;
import com.bayfi.entity.User;
import com.bayfi.exception.UserAlreadyExistsException;
import com.bayfi.repository.UserRepository;
import com.bayfi.service.AuthService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

@Transactional
@Service
public class AuthServiceImpl implements AuthService {

    private final static Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<String> registerUser(SignUpRequest request) {
        logger.info("Registering user with email: {}", request.getEmail());

        // Check if user already exists
        Optional<User> existingUserByEmail = userRepository.findByEmail(request.getEmail().toLowerCase());

        Optional<User> existingUserByUsername = userRepository.findByUsername(request.getUsername().toUpperCase());

        if (existingUserByUsername.isPresent()) {
            logger.warn("User registration failed: username already exists {}", existingUserByUsername.get().getUsername());
            throw new UserAlreadyExistsException("Username already exists: " + existingUserByUsername.get().getUsername());
        }

        if (existingUserByEmail.isPresent()) {
            logger.warn("User registration failed: email already exists {}", existingUserByEmail.get().getEmail());
            throw new UserAlreadyExistsException("Email already exists: " + existingUserByEmail.get().getEmail());
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        logger.info("User details: {}", request);

        // Create new user
        User newUser = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail().toLowerCase())
                .username(request.getUsername().toUpperCase())
                .password(encodedPassword)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();

        try {
            // Save user to database
            logger.info("Saving new user: {}", newUser);
            User savedUser = userRepository.save(newUser);
            logger.info("User registered successfully with ID: {}", savedUser.getId());

            // Build the Location header
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedUser.getId())
                    .toUri();

            // Return 201 Created with the Location header and a success message
            return ResponseEntity.created(location).body("User registered successfully!: " + location);
        } catch (RuntimeException ex) {
            // Handle other exception
            logger.error("User Registration failed: {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }
}