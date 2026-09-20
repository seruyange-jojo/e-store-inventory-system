package com.joseph.ismes.controller;

import com.joseph.ismes.exception.GlobalExceptionHandler;
import com.joseph.ismes.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private AuthenticationManager authenticationManager;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(authenticationManager, mock(JwtUtil.class)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("incorrect password"));
        login().andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value("Invalid username or password"));
    }

    @Test
    void disabledAccountReturns401WithoutExposingAccountState() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));
        login().andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value("Invalid username or password"));
    }

    @Test
    void authenticationInfrastructureFailureReturns500() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new InternalAuthenticationServiceException("private database connection details"));
        login().andExpect(status().isInternalServerError())
                .andExpect(jsonPath("message").value("An unexpected error occurred"));
    }

    @Test
    void malformedJsonReturns400BeforeAuthentication() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("status").value(400));
        verifyNoInteractions(authenticationManager);
    }

    private org.springframework.test.web.servlet.ResultActions login() throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"));
    }
}
