package com.camerashop.config;

import com.camerashop.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 authentication success handler.
 */
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    public OAuth2SuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        // Extract user info from OAuth2 provider
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");

        if (email == null) {
            // Try alternative attribute names for different providers
            email = (String) attributes.get("email_address");
        }

        if (name == null) {
            // Combine first and last name if available
            String firstName = (String) attributes.get("first_name");
            String lastName = (String) attributes.get("last_name");
            if (firstName != null && lastName != null) {
                name = firstName + " " + lastName;
            } else if (firstName != null) {
                name = firstName;
            }
        }

        // Register or login the user
        var authResponse = authService.registerOAuthUser(email, name, provider, providerId);

        // Redirect to frontend with token as query parameter
        // For web: http://localhost:8081/oauth-success?token=...
        // For mobile (Expo): mobile://oauth-success?token=...
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null || frontendUrl.isEmpty()) {
            // Default to localhost for development
            frontendUrl = "http://localhost:8081";
        }

        // Encode token to handle special characters
        String encodedToken = URLEncoder.encode(authResponse.getToken(), StandardCharsets.UTF_8);
        String redirectUrl = frontendUrl + "/oauth-success?token=" + encodedToken;

        System.out.println("OAuth login successful, redirecting to: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
