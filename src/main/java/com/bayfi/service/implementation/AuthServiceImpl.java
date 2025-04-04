package com.bayfi.service.implementation;

import com.bayfi.dto.request.SignInRequest;
import com.bayfi.dto.request.SignUpRequest;
import com.bayfi.entity.Role;
import com.bayfi.entity.User;
import com.bayfi.exception.InvalidCredentialException;
import com.bayfi.exception.UserAlreadyExistsException;
import com.bayfi.repository.RoleRepository;
import com.bayfi.repository.UserRepository;
import com.bayfi.service.AuthService;
import com.bayfi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

@Transactional
@Service
public class AuthServiceImpl implements AuthService {

    private final static Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    private  JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public URI registerUser(SignUpRequest request) {
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

        //Initialize roles if null
        if(newUser.getRoles() == null){
            newUser.setRoles(new HashSet<>());
        }

        Optional<Role> userRole = roleRepository.findByAuthority("ROLE_USER");
        userRole.ifPresent(role -> newUser.getRoles().add(role));

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

//            return ResponseEntity.created(location).body("User registered successfully!: " + location);
            return location;
        } catch (RuntimeException ex) {
            // Handle other exception
            logger.error("User Registration failed: {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }



    @Override
    public String authenticateUser(SignInRequest signInRequest){

        Optional<User> existingUserByEmail = userRepository.findByEmail(signInRequest.getEmail().toLowerCase());

        if(existingUserByEmail.isEmpty()){
            logger.warn("User not found with email: {}", signInRequest.getEmail().toLowerCase());
            throw new UsernameNotFoundException("invalid email or password");
        }


        try{
            logger.info("attempting to authenticate with details {}" , signInRequest);
            Authentication authentication = authenticationManager.authenticate(

                    new UsernamePasswordAuthenticationToken(signInRequest.getEmail().toLowerCase(), signInRequest.getPassword())
            );
            logger.info("User authenticated successfully. Generating JWT token...");

            // return jwt token.
            return jwtUtil.generateJwt(authentication, signInRequest.getEmail().toLowerCase());

        }catch(RuntimeException e){
            logger.error("Unexpected Error :  {}", e.getMessage());
            throw new InvalidCredentialException("invalid email or password");
        }

    }



}