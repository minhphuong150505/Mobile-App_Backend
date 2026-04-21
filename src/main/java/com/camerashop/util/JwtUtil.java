package com.camerashop.util;

import com.camerashop.entity.User;
import com.camerashop.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Autowired
    private JwtService jwtService;

    public String extractUsername(String token) {
        return jwtService.extractUsername(token);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        return jwtService.isTokenValid(token, userDetails);
    }

    public String generateToken(UserDetails userDetails) {
        // For backwards compatibility, create a User-like object from UserDetails
        // This allows existing code using email-based tokens to continue working
        User user = User.builder()
                .userId(userDetails.getUsername()) // Use email as userId for compatibility
                .userName(userDetails.getUsername())
                .email(userDetails.getUsername())
                .role(User.Role.USER)
                .build();
        return jwtService.generateToken(user);
    }
}
