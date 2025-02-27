package com.wallet.auth.service;

import com.wallet.auth.DTO.JWTToken;
import com.wallet.auth.DTO.LoginRequest;
import com.wallet.auth.DTO.RegisterRequest;
import com.wallet.auth.exception.UserAlreadyExistsException;
import com.wallet.auth.exception.UserNotExistsException;
import com.wallet.auth.exception.WrongCredentialException;
import com.wallet.auth.model.User;
import com.wallet.auth.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@AutoConfigureMockMvc(addFilters = false)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEventPublisher userEventPublisher;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId("12345");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        String response = authService.register(request);


        assertEquals("User registered successfully!", response);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userEventPublisher, times(1)).publishUserCreatedEvent(user);
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.ofNullable(user));

        assertThrows(UserAlreadyExistsException.class,()->authService.register(request));
        verify(userRepository, never()).save(any(User.class));
        verify(userEventPublisher, never()).publishUserCreatedEvent(any(User.class));
    }


    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("test@example.com", "encodedPassword");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.ofNullable(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("failedAttempts:" + user.getEmail())).thenReturn("0");

        try (MockedStatic<JWTUtil> mockedJwtUtil = mockStatic(JWTUtil.class)) {
            mockedJwtUtil.when(() -> JWTUtil.generateToken(user)).thenReturn("mock.jwt.token");

            JWTToken response = authService.login(request);

            assertNotNull(response);
            assertEquals("mock.jwt.token", response.getToken());
        }
    }

    @Test
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotExistsException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.ofNullable(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(WrongCredentialException.class, () -> authService.login(request));
    }

    @Test
    void testGetProfile_Success() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String mockToken = "Bearer mock.jwt.token";
        String userId = user.getId();

        when(mockRequest.getHeader("Authorization")).thenReturn(mockToken);

        try (MockedStatic<JWTUtil> mockedJwtUtil = mockStatic(JWTUtil.class)) {
            mockedJwtUtil.when(() -> JWTUtil.extractUserId("mock.jwt.token")).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            User response = authService.getProfile(mockRequest);

            assertNotNull(response);
            assertEquals(user, response);
        }
    }


    @Test
    void testDeleteUser_Success() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String mockToken = "Bearer mock.jwt.token";
        String userId = user.getId();

        when(mockRequest.getHeader("Authorization")).thenReturn(mockToken);

        try (MockedStatic<JWTUtil> mockedJwtUtil = mockStatic(JWTUtil.class)) {
            mockedJwtUtil.when(() -> JWTUtil.extractUserId("mock.jwt.token")).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            String response = authService.delete(mockRequest);

            assertEquals("User deleted successfully!", response);
            verify(userRepository, times(1)).deleteById(userId);
            verify(userEventPublisher, times(1)).publishUserDeletedEvent(user);
        }
    }

    @Test
    void testDeleteUser_UserNotFound() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String mockToken = "Bearer mock.jwt.token";

        when(mockRequest.getHeader("Authorization")).thenReturn(mockToken);

        try (MockedStatic<JWTUtil> mockedJwtUtil = mockStatic(JWTUtil.class)) {
            mockedJwtUtil.when(() -> JWTUtil.extractUserId("mock.jwt.token")).thenReturn("unknownId");
            when(userRepository.findById("unknownId")).thenReturn(Optional.empty());

            assertThrows(UserNotExistsException.class, () -> authService.delete(mockRequest));
            verify(userRepository, never()).deleteById(anyString());
        }
    }


}
