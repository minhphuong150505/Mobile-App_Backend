package com.camerashop.service;

import com.camerashop.dto.*;
import com.camerashop.entity.EmailVerificationToken;
import com.camerashop.entity.User;
import com.camerashop.entity.User.Role;
import com.camerashop.repository.UserRepository;
import com.camerashop.util.JwtUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailVerificationTokenService tokenService;

    public AuthResponse register(RegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already registered");
            }

            User user = User.builder()
                    .userName(request.getUserName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.USER)
                    .trustScore(100)
                    .emailVerified(false)
                    .provider("local")
                    .build();

            userRepository.save(user);
            System.out.println("User saved: " + user.getEmail());

            // Create verification token (fail-safe)
            EmailVerificationToken token = null;
            try {
                token = tokenService.createVerificationToken(user);
                System.out.println("Verification token created: " + token.getToken());
            } catch (Exception e) {
                System.err.println("Failed to create verification token: " + e.getMessage());
            }

            // Send verification email (fail-safe - don't fail registration if email fails)
            if (token != null) {
                try {
                    emailService.sendEmailVerification(user.getEmail(), user.getUserName(), token.getToken());
                    System.out.println("Verification email sent");
                } catch (Exception e) {
                    // Log error but don't fail registration - email service might not be configured
                    System.err.println("Failed to send verification email (this is OK for development): " + e.getMessage());
                }
            }

            // Generate JWT token directly using email as subject
            var userDetails = new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    java.util.Collections.singletonList(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                    )
            );
            System.out.println("UserDetails created: " + userDetails.getUsername());

            String jwtToken = jwtUtil.generateToken(userDetails);
            System.out.println("JWT token generated successfully");

            return AuthResponse.builder()
                    .token(jwtToken)
                    .email(user.getEmail())
                    .userName(user.getUserName())
                    .role(user.getRole().name())
                    .userId(user.getUserId())
                    .emailVerified(false)
                    .message("Registration successful. Please check your email to verify your account.")
                    .build();
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    public AuthResponse registerOAuthUser(String email, String name, String provider, String providerId) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Create new user
            user = User.builder()
                    .userName(name)
                    .email(email)
                    .password(null) // No password for OAuth users
                    .role(Role.USER)
                    .trustScore(100)
                    .emailVerified(true) // OAuth emails are pre-verified
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(user);
        } else {
            // Update existing user with OAuth info if needed
            if (user.getProvider() == null) {
                user.setProvider(provider);
                user.setProviderId(providerId);
                user.setEmailVerified(true);
                userRepository.save(user);
            }
        }

        String token = jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password("")
                        .roles(user.getRole().name())
                        .build()
        );

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .userName(user.getUserName())
                .role(user.getRole().name())
                .userId(user.getUserId())
                .emailVerified(true)
                .build();
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenService.getVerificationToken(token);

        if (verificationToken == null) {
            throw new RuntimeException("Invalid verification token");
        }

        if (tokenService.isTokenExpired(verificationToken)) {
            throw new RuntimeException("Verification token has expired");
        }

        if (verificationToken.isUsed()) {
            throw new RuntimeException("Token has already been used");
        }

        tokenService.confirmVerification(verificationToken);
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        EmailVerificationToken token = tokenService.createVerificationToken(user);
        try {
            emailService.sendEmailVerification(user.getEmail(), user.getUserName(), token.getToken());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        String token = jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build()
        );

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .userName(user.getUserName())
                .role(user.getRole().name())
                .userId(user.getUserId())
                .build();
    }

    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .trustScore(user.getTrustScore())
                .build();
    }

    public UserDTO updateAvatar(String email, String avatarUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return UserDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .trustScore(user.getTrustScore())
                .build();
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
