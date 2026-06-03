package com.waypoint.service;

import com.waypoint.dto.*;
import com.waypoint.entity.RefreshToken;
import com.waypoint.entity.Role;
import com.waypoint.entity.User;
import com.waypoint.exception.BadRequestException;
import com.waypoint.exception.ResourceNotFoundException;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.UserRepository;
import com.waypoint.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EntityMapper entityMapper;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        return buildJwtResponse(user);
    }

    @Transactional
    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildJwtResponse(user);
    }

    @Transactional
    public RefreshTokenResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenService.validateRefreshToken(refreshToken);
        refreshTokenService.revokeRefreshToken(stored);

        User user = stored.getUser();
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUsername(), user.getRole().name(), user.getId());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }

    private JwtResponse buildJwtResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUsername(), user.getRole().name(), user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return JwtResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .id(user.getId())
                .build();
    }
}
