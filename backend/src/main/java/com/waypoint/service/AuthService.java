package com.waypoint.service;

import com.waypoint.dto.*;
import com.waypoint.entity.Role;
import com.waypoint.entity.User;
import com.waypoint.exception.BadRequestException;
import com.waypoint.exception.ResourceNotFoundException;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.UserRepository;
import com.waypoint.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EntityMapper entityMapper;

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

        String token = jwtTokenProvider.generateToken(
                user.getUsername(), user.getRole().name(), user.getId());

        return JwtResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .id(user.getId())
                .build();
    }

    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtTokenProvider.generateToken(
                user.getUsername(), user.getRole().name(), user.getId());

        return JwtResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .id(user.getId())
                .build();
    }
}
