package com.wallet.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.auth.DTO.JWTToken;
import com.wallet.auth.DTO.LoginRequest;
import com.wallet.auth.DTO.RegisterRequest;
import com.wallet.auth.exception.GlobalExceptionHandler;
import com.wallet.auth.exception.UserAlreadyExistsException;
import com.wallet.auth.exception.UserNotExistsException;
import com.wallet.auth.exception.WrongCredentialException;
import com.wallet.auth.model.User;
import com.wallet.auth.repository.UserRepository;
import com.wallet.auth.service.AuthService;

import com.wallet.auth.service.UserEventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserEventPublisher userEventPublisher;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        registerRequest = new RegisterRequest("Test User", "test@example.com", "password");
        loginRequest = new LoginRequest("test@example.com", "password");
        user = new User();
        user.setId("123");
        user.setEmail("test@example.com");
        user.setPassword("rawPassword");
    }
    @Test
    void testRegister_EmailNotEmpty() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(""); // Blank email to trigger validation
        registerRequest.setPassword("Test@123");
        registerRequest.setName("Test User");

        MvcResult result =mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest()).andReturn();

        MethodArgumentNotValidException exception =
                (MethodArgumentNotValidException) result.getResolvedException();

        assert exception != null;
        String errorMessage = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        assertEquals("Email cannot be blank", errorMessage);
    }

    @Test
    void testRegister_NameNotEmpty() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("Test@example.com"); // Blank email to trigger validation
        registerRequest.setPassword("Test@123");
        registerRequest.setName("");

        MvcResult result =mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest()).andReturn();

        MethodArgumentNotValidException exception =
                (MethodArgumentNotValidException) result.getResolvedException();

        assert exception != null;
        String errorMessage = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        assertEquals("Name cannot be blank", errorMessage);
    }

    @Test
    void testRegister_Success() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
               .thenReturn("User registered successfully!");


        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully!"));
       verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testRegister_UserAlreadyExists() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User Already Exist!"));


        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Exception resolvedException = result.getResolvedException();
                    assert resolvedException != null;
                    assert resolvedException.getMessage().equals("User Already Exist!"); // ✅ Compare message
                });

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testLogin_Success() throws Exception {
        JWTToken mockToken = new JWTToken("mockValidToken"); // ✅ Mocking the JWT token response

        when(authService.login(any(LoginRequest.class))).thenReturn(mockToken); // ✅ Using `thenAnswer`

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mockValidToken")); // ✅ Checking token field

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new WrongCredentialException("Invalid credentials!"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> {
                    Exception resolvedException = result.getResolvedException();
                    assert resolvedException != null;
                    assert resolvedException.getMessage().equals("Invalid credentials!"); // ✅ Compare message
                });
        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testGetProfile_Success() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(authService.getProfile(any(HttpServletRequest.class))).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/profile")
                        .header("Authorization", "Bearer mock.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
        verify(authService, times(1)).getProfile(any(HttpServletRequest.class));
    }

    @Test
    void testDelete_Success() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(authService.delete(any(HttpServletRequest.class))).thenReturn("User deleted successfully!");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/auth/delete")
                        .header("Authorization", "Bearer mock.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully!"));
        verify(authService, times(1)).delete(any(HttpServletRequest.class));
    }

    @Test
    void testDelete_UserNotFound() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(authService.delete(any(HttpServletRequest.class))).thenThrow(new UserNotExistsException("User not exists!"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/auth/delete")
                        .header("Authorization", "Bearer mock.jwt.token"))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).delete(any(HttpServletRequest.class));
    }
}
