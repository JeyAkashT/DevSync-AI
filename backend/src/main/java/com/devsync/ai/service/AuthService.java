package com.devsync.ai.service;

import com.devsync.ai.api.dto.auth.AuthResponse;
import com.devsync.ai.api.dto.auth.LoginRequest;
import com.devsync.ai.api.dto.auth.RegisterRequest;
import com.devsync.ai.api.dto.auth.UserResponse;
import com.devsync.ai.config.JwtProperties;
import com.devsync.ai.exception.EmailConflictException;
import com.devsync.ai.model.Role;
import com.devsync.ai.model.User;
import com.devsync.ai.repository.RoleRepository;
import com.devsync.ai.repository.UserRepository;
import com.devsync.ai.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final JwtTokenService jwtTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest dto) {
        String emailNorm = dto.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(emailNorm)) {
            throw new EmailConflictException(emailNorm);
        }

        Role userRole =
                roleRepository.findByName("USER").orElseThrow(() -> new IllegalStateException("USER role missing"));

        User user = new User();
        user.setEmail(emailNorm);
        user.setFullName(dto.fullName() != null ? dto.fullName().trim() : null);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.addRole(userRole);

        User saved = userRepository.save(user);
        User hydrated = userRepository.findByIdFetchRoles(saved.getId()).orElseThrow();
        return toAuthResponse(hydrated);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest dto) {
        String emailNorm = dto.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository
                .findByEmailFetchRoles(emailNorm)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        String hash = user.getPasswordHash();
        if (hash == null || !passwordEncoder.matches(dto.password(), hash)) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String jwt = jwtTokenService.createAccessToken(user);
        return new AuthResponse(
                jwt,
                "Bearer",
                jwtProperties.getExpirationSeconds(),
                UserResponse.fromEntity(user));
    }
}
