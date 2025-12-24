package com.hrtrack.utils;


import com.hrtrack.domain.Role;
import com.hrtrack.domain.entities.User;
import com.hrtrack.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    private static final String ADMIN_URL = "http://localhost:3000/administration/admin/dashboard";
    private static final String RH_URL = "http://localhost:3000/administration/rh/dashboard";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {


        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();
        String email = (String) attributes.get("email");

        String targetUrl = "";

        if (isAdmin(email)) {
            targetUrl = ADMIN_URL;
        } else if (isRh(email)) {
            targetUrl = RH_URL;
        }
        response.sendRedirect(targetUrl);
    }


    private boolean isAdmin(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email)
        );

        return user.getRole().equals(Role.ADMIN);
    }

    private boolean isRh(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email)
        );

        return user.getRole().equals(Role.RH);
    }
}
