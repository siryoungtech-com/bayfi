package com.bayfi.service.implementation;

import com.bayfi.entity.Oauth2AuthenticatonProvider;
import com.bayfi.entity.Role;
import com.bayfi.entity.User;
import com.bayfi.enums.Oauth2ProviderType;
import com.bayfi.repository.Oauth2ProviderRepository;
import com.bayfi.repository.RoleRepository;
import com.bayfi.repository.UserRepository;
import com.bayfi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Slf4j
@Transactional
@Service
public class Oauth2LoginService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final Oauth2ProviderRepository oauth2ProviderRepository;

    public Oauth2LoginService(ClientRegistrationRepository clientRegistrationRepository, UserRepository userRepository, JwtUtil jwtUtil, RoleRepository roleRepository, Oauth2ProviderRepository oauth2ProviderRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
        this.oauth2ProviderRepository = oauth2ProviderRepository;
    }

    public String getAuthorizationUrl(String provider){
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(provider);

        if(clientRegistration == null){
            throw new IllegalArgumentException("Invalid provider:" + provider);
        }

        String state = UUID.randomUUID().toString();

        String authorizationUri = clientRegistration.getProviderDetails().getAuthorizationUri();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(authorizationUri)
                .queryParam("scope", String.join(" ", clientRegistration.getScopes()))
                .queryParam("access_type", "offline") // Ensure refresh token for Google
                .queryParam("include_granted_scopes", "true")
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("redirect_uri", clientRegistration.getRedirectUri())
                .queryParam("client_id", clientRegistration.getClientId()) ;

        log.info("Generated Authorization URL: {}", uriBuilder.toUriString());


        return uriBuilder.toUriString();
    }


    public String exchangeCodeForJwt(String provider, String code) {

        Oauth2ProviderType providerType = Oauth2ProviderType.valueOf(provider.toUpperCase());

        ClientRegistration clientRegistration = ((InMemoryClientRegistrationRepository) clientRegistrationRepository)
                .findByRegistrationId(provider);

        if (clientRegistration == null) {
            throw new IllegalArgumentException("Invalid provider: " + provider);
        }

        // Exchange the authorization code for an access token
        String tokenUrl = clientRegistration.getProviderDetails().getTokenUri();
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("client_id", clientRegistration.getClientId());
        requestParams.add("client_secret", clientRegistration.getClientSecret());
        requestParams.add("code", code);
        requestParams.add("grant_type", "authorization_code");
        requestParams.add("redirect_uri", clientRegistration.getRedirectUri());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestParams, headers);
        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

        String accessToken = (String) Objects.requireNonNull(response.getBody()).get("access_token");

        // Fetch user details
        OAuth2User user = fetchUserDetails(provider, accessToken);
        String email = user.getAttribute("email");

        // Check if user exists or create a new one
        User existingUser = userRepository.findByEmail(email)
                .orElseGet(() -> registerOrLinkUser(providerType, user));

        // Generate JWT
        Authentication auth = new UsernamePasswordAuthenticationToken(existingUser.getEmail(), null, existingUser.getAuthorities());
        return jwtUtil.generateJwt(auth, existingUser.getEmail());
    }


    private OAuth2User fetchUserDetails(String provider, String accessToken) {

        String userInfoUrl = OAUTH2_USER_INFO_URLS.get(provider);

        if (userInfoUrl == null) {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        return new DefaultOAuth2User(Collections.emptyList(), response.getBody(), "email");
    }


    private User registerOrLinkUser(Oauth2ProviderType oauth2ProviderType, OAuth2User oAuth2User) {
        log.info("OAuth2 user info: {}", oAuth2User.getAttributes());

        String email = oAuth2User.getAttribute("email");
        String providerId = getAttributeValue(oAuth2User, "sub", "id"); // Unique provider ID

        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        User user = existingUserOpt.orElseGet(() -> {
            // Register a new user
            User newUser = new User();
            newUser.setEmail(email);
            String firstName = getAttributeValue(oAuth2User, "given_name", "first_name", "name");
            String lastName = getAttributeValue(oAuth2User, "family_name", "last_name", "name");
            newUser.setLastname(lastName);
            newUser.setFirstname(firstName);

            // Set a random secure password (since password-based auth is not used)
            String randomPassword = UUID.randomUUID().toString();
            newUser.setPassword(new BCryptPasswordEncoder().encode(randomPassword));

            // Ensure unique username (fallback to email if necessary)
            newUser.setUsername(generateUniqueUsername(firstName, lastName));

            // Assign default role
            roleRepository.findByAuthority("ROLE_USER").ifPresent(newUser.getRoles()::add);

            // Save user before linking provider
            return userRepository.save(newUser);
        });

        // Check if the provider is already linked to this user
        boolean providerExists = user.getOauth2AuthenticatonProviders().stream()
                .anyMatch(authProvider -> authProvider.getOauth2ProviderType() == oauth2ProviderType);

        if (!providerExists) {
            // Link new authentication provider
            Oauth2AuthenticatonProvider authProvider = Oauth2AuthenticatonProvider.builder()
                    .oauth2ProviderType(oauth2ProviderType)
                    .providerId(providerId)
                    .user(user)
                    .build();

            oauth2ProviderRepository.save(authProvider);
            user.getOauth2AuthenticatonProviders().add(authProvider);
        }

        return user;
    }





    private static final Map<String, String> OAUTH2_USER_INFO_URLS = Map.of(
            "google", "https://www.googleapis.com/oauth2/v3/userinfo",
            "facebook", "https://graph.facebook.com/me?fields=email,first_name,last_name",
            "apple", "https://appleid.apple.com/auth/userinfo"
    );

    private String getAttributeValue(OAuth2User user, String... possibleKeys) {
        for (String key : possibleKeys) {
            Object value = user.getAttribute(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    private String generateUniqueUsername(String firstName, String lastName) {
        // Default to "user" if names are not available
        String baseUsername = (firstName != null ? firstName : "user") + (lastName != null ? lastName.substring(0, 1) : "X");

        // Append a random 3-digit number for uniqueness
        return baseUsername.toUpperCase() + new Random().nextInt(900) + 100;

    }

}
