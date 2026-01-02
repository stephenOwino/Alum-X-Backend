package com.opencode.alumxbackend.auth.service;

import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.auth.exception.InvalidCredentialsException;
import com.opencode.alumxbackend.auth.security.JwtTokenProvider;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // Find user by email or username
        User user = userRepository.findByEmail(loginRequest.getEmailOrUsername())
                .orElseGet(() -> userRepository.findByUsername(loginRequest.getEmailOrUsername())
                        .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials")));

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Generate token
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );

        // Build response
        LoginResponse.UserBasicInfo userInfo = new LoginResponse.UserBasicInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );

        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setTokenExpiryTime(jwtTokenProvider.getExpirationTime());
        response.setUser(userInfo);

        return response;
    }
}
