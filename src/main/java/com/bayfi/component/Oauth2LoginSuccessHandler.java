//package com.bayfi.component;
//
//import com.bayfi.service.UserService;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//
//@Component
//public class Oauth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final UserService userService;
//
//
//    // Constructor injection
//    public Oauth2LoginSuccessHandler(UserService userService) {
//        this.userService = userService;
//    }
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException, ServletException {
//        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
//
////        TODO: check if user exist
//
//        // Save or update user in the database
//        String email = oauth2User.getAttribute("email");
//        String provider = oauth2User.getAttribute("provider");
//        String providerId = oauth2User.getAttribute("sub");
//
//        userService.processOauth2User(email, provider, providerId);
//
//        // Redirect to the home page or dashboard
//        getRedirectStrategy().sendRedirect(request, response, "/dashboard");
//    }
//}