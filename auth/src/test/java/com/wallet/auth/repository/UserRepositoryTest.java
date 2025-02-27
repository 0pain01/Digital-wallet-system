package com.wallet.auth.repository;

import com.wallet.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ExtendWith(SpringExtension.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean up before each test
    }

    @Test
    void testFindByEmail_UserExists() {
        User user = new User("1", "John Doe", "john@example.com", "password");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("john@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testFindByEmail_UserNotFound() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertThat(foundUser).isEmpty();
    }

    @Test
    void testSaveUser() {
        User user = new User("2", "Jane Doe", "jane@example.com", "password123");
        User savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void testDeleteUser() {
        User user = new User("3", "Alice", "alice@example.com", "securePass");
        userRepository.save(user);

        userRepository.deleteById(user.getId());
        Optional<User> foundUser = userRepository.findByEmail("alice@example.com");

        assertThat(foundUser).isEmpty();
    }
}
