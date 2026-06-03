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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private EntityMapper entityMapper;
    @Mock private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider,
                authenticationManager, entityMapper, refreshTokenService);
    }

    @Test
    void register_createsUserAndReturnsJwt() {
        RegisterRequest request = new RegisterRequest("newuser", "new@test.com", "password123");
        User savedUser = User.builder()
                .id(1L).username("newuser").email("new@test.com")
                .password("encoded").role(Role.USER).build();
        RefreshToken refreshToken = RefreshToken.builder().token("rt-uuid").build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateAccessToken("newuser", "USER", 1L)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(savedUser)).thenReturn(refreshToken);

        JwtResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("rt-uuid");
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getRole()).isEqualTo("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("existing", "new@test.com", "password123");
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void register_throwsWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("newuser", "dup@test.com", "password123");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already exists");
    }

    @Test
    void login_authenticatesAndReturnsJwt() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = User.builder()
                .id(1L).username("testuser").email("test@test.com")
                .password("encoded").role(Role.USER).build();
        RefreshToken refreshToken = RefreshToken.builder().token("rt-uuid").build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken("testuser", "USER", 1L)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        JwtResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("rt-uuid");
        assertThat(response.getUsername()).isEqualTo("testuser");
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("testuser", "password123"));
    }

    @Test
    void login_throwsWhenUserNotFound() {
        LoginRequest request = new LoginRequest("nobody", "password123");
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void refresh_validatesOldTokenCreatesNewPair() {
        User user = User.builder().id(1L).username("testuser").role(Role.USER).build();
        RefreshToken oldToken = RefreshToken.builder().token("old-rt").user(user).build();
        RefreshToken newToken = RefreshToken.builder().token("new-rt").build();

        when(refreshTokenService.validateRefreshToken("old-rt")).thenReturn(oldToken);
        when(jwtTokenProvider.generateAccessToken("testuser", "USER", 1L)).thenReturn("new-access");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newToken);

        RefreshTokenResponse response = authService.refresh("old-rt");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-rt");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(refreshTokenService).revokeRefreshToken(oldToken);
    }
}
