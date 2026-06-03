package com.waypoint.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "WaypointTestSecretKeyForJWTTokenGeneration2024MustBe256BitsLongMin",
                1800000L);
    }

    @Test
    void generateAndValidateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken("testuser", "USER", 1L);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("testuser");
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo("USER");
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(1L);
    }

    @Test
    void generateTokenWithCustomExpiration() {
        String token = jwtTokenProvider.generateToken("testuser", "USER", 1L, 60000L);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_returnsFalseForInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void validateToken_returnsFalseForNull() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }
}
