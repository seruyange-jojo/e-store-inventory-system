package com.joseph.ismes.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joseph.ismes.entity.Role;
import com.joseph.ismes.entity.User;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final String SECRET = "local-test-secret-that-is-at-least-thirty-two-bytes";
    @Mock private UserDetailsService users;
    @Mock private FilterChain chain;
    private final JwtUtil jwt = new JwtUtil(SECRET, 60_000);
    private JwtAuthFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private User user;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwt, users,
                new ApiSecurityErrorHandler(new ObjectMapper().findAndRegisterModules()));
        request = new MockHttpServletRequest("GET", "/api/products");
        request.setServletPath("/api/products");
        response = new MockHttpServletResponse();
        user = User.builder().username("admin").passwordHash("unused").role(Role.ADMIN).active(true).build();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenLoadsCurrentUserAndAuthenticates() throws Exception {
        request.addHeader("Authorization", "Bearer " + jwt.generateToken(user));
        when(users.loadUserByUsername("admin")).thenReturn(user);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(user);
        verify(chain).doFilter(request, response);
    }

    @Test
    void malformedTokenReturnsJson401WithoutContinuing() throws Exception {
        request.addHeader("Authorization", "Bearer broken-token");
        filter.doFilter(request, response, chain);
        assertUnauthorized();
        verifyNoInteractions(users);
    }

    @Test
    void expiredTokenReturnsJson401WithoutLookingUpUser() throws Exception {
        String expired = new JwtUtil(SECRET, -60_000).generateToken(user);
        request.addHeader("Authorization", "Bearer " + expired);
        filter.doFilter(request, response, chain);
        assertUnauthorized();
        verifyNoInteractions(users);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/login", "/actuator/health", "/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs"})
    void staleTokenDoesNotBlockPublicEndpoints(String path) throws Exception {
        request.setServletPath(path);
        request.setRequestURI(path);
        request.addHeader("Authorization", "Bearer broken-token");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(users);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void tokenForDeletedUserReturns401() throws Exception {
        request.addHeader("Authorization", "Bearer " + jwt.generateToken(user));
        when(users.loadUserByUsername("admin")).thenThrow(new UsernameNotFoundException("missing"));
        filter.doFilter(request, response, chain);
        assertUnauthorized();
    }

    @Test
    void tokenForDisabledUserReturns401() throws Exception {
        request.addHeader("Authorization", "Bearer " + jwt.generateToken(user));
        user.setActive(false);
        when(users.loadUserByUsername("admin")).thenReturn(user);
        filter.doFilter(request, response, chain);
        assertUnauthorized();
    }

    @Test
    void databaseFailuresAreNotDisguisedAsInvalidCredentials() {
        request.addHeader("Authorization", "Bearer " + jwt.generateToken(user));
        when(users.loadUserByUsername("admin")).thenThrow(new DataAccessResourceFailureException("database unavailable"));

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(DataAccessResourceFailureException.class);
        assertThat(response.getStatus()).isNotEqualTo(401);
    }

    @Test
    void downstreamApplicationFailuresAreNotDisguisedAsInvalidTokens() throws Exception {
        request.addHeader("Authorization", "Bearer " + jwt.generateToken(user));
        when(users.loadUserByUsername("admin")).thenReturn(user);
        doThrow(new IllegalArgumentException("application error")).when(chain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(response.getStatus()).isNotEqualTo(401);
    }

    private void assertUnauthorized() {
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsByteArray()).isNotEmpty();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(chain);
    }
}
