package com.waypoint.repository;

import com.waypoint.entity.Role;
import com.waypoint.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_returnsUser() {
        User user = User.builder()
                .username("findme").email("findme@test.com")
                .password("encoded").role(Role.USER).build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("findme");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("findme");
    }

    @Test
    void findByUsername_returnsEmptyWhenNotFound() {
        Optional<User> found = userRepository.findByUsername("nobody");

        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_returnsUser() {
        User user = User.builder()
                .username("emailuser").email("email@test.com")
                .password("encoded").role(Role.USER).build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("email@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("email@test.com");
    }

    @Test
    void existsByUsername_returnsTrueWhenExists() {
        User user = User.builder()
                .username("exists").email("exists@test.com")
                .password("encoded").role(Role.USER).build();
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("exists")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    void existsByEmail_returnsTrueWhenExists() {
        User user = User.builder()
                .username("emailexists").email("emailexists@test.com")
                .password("encoded").role(Role.USER).build();
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("emailexists@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@test.com")).isFalse();
    }
}
