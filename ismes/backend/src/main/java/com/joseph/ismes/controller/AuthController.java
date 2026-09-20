package com.joseph.ismes.controller;

import com.joseph.ismes.dto.LoginRequest;
import com.joseph.ismes.dto.LoginResponse;
import com.joseph.ismes.entity.User;
import com.joseph.ismes.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User user;
        try {
            var authResult = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            user = (User) authResult.getPrincipal();
        } catch (BadCredentialsException | AccountStatusException | UsernameNotFoundException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user);

        return new LoginResponse(token, user.getUsername(), user.getFullName(), user.getRole().name());
    }
}
