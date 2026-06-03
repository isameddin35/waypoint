package com.waypoint.service;

import com.waypoint.entity.RefreshToken;
import com.waypoint.entity.Role;
import com.waypoint.entity.User;
import com.waypoint.exception.BadRequestException;
import com.waypoint.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void createRefreshToken_createsAndReturnsToken() {
        User user = User.builder().id(1L).username("testuser").build();
        RefreshToken saved = RefreshToken.builder()
                .id(1L).token("uuid").user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(saved);

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertThat(result.getToken()).isEqualTo("uuid");
        verify(refreshTokenRepository).revokeByUserId(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_returnsTokenWhenValid() {
        RefreshToken token = RefreshToken.builder()
                .token("valid-token")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.validateRefreshToken("valid-token");

        assertThat(result.getToken()).isEqualTo("valid-token");
    }

    @Test
    void validateRefreshToken_throwsWhenNotFound() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("missing"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void validateRefreshToken_throwsWhenRevoked() {
        RefreshToken token = RefreshToken.builder()
                .token("revoked-token")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("revoked-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Refresh token has been revoked");
    }

    @Test
    void validateRefreshToken_throwsWhenExpired() {
        RefreshToken token = RefreshToken.builder()
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("expired-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Refresh token has expired");
    }

    @Test
    void revokeRefreshToken_marksAsRevoked() {
        RefreshToken token = RefreshToken.builder().token("rt").revoked(false).build();

        refreshTokenService.revokeRefreshToken(token);

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }
}
