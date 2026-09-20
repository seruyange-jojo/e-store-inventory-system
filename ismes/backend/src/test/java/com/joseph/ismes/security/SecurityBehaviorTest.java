package com.joseph.ismes.security;

import com.joseph.ismes.config.SecurityConfig;
import com.joseph.ismes.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SecurityBehaviorTest.ProbeController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, ApiSecurityErrorHandler.class,
        GlobalExceptionHandler.class, SecurityBehaviorTest.ProbeController.class})
class SecurityBehaviorTest {

    @Autowired private MockMvc mvc;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    void unauthenticatedProtectedRequestReturnsJson401() throws Exception {
        mvc.perform(get("/api/probe")).andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("status").value(401));
    }

    @Test
    @WithMockUser(roles = "SALES")
    void insufficientRoleReturnsJson403InsteadOf500() throws Exception {
        mvc.perform(get("/api/probe/admin")).andExpect(status().isForbidden())
                .andExpect(jsonPath("status").value(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administratorCanUseRestrictedEndpoint() throws Exception {
        mvc.perform(get("/api/probe/admin")).andExpect(status().isOk());
    }

    @Test
    void advertisedSwaggerEntrypointIsPublicEvenWithAStaleToken() throws Exception {
        mvc.perform(get("/swagger-ui.html").header("Authorization", "Bearer broken-token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void invalidPathParameterReturns400() throws Exception {
        mvc.perform(get("/api/probe/id/invalid")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(400));
    }

    @RestController
    static class ProbeController {
        @GetMapping("/api/probe")
        String authenticated() { return "ok"; }

        @GetMapping("/api/probe/admin")
        @PreAuthorize("hasRole('ADMIN')")
        String administrator() { return "ok"; }

        @GetMapping("/swagger-ui.html")
        String documentation() { return "docs"; }

        @GetMapping("/api/probe/id/{id}")
        Long identifier(@PathVariable Long id) { return id; }
    }
}
