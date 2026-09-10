package com.marketlens.auth;

import java.util.Set;
import java.util.UUID;

import com.marketlens.auth.dto.AuthResponse;
import com.marketlens.auth.dto.LoginRequest;
import com.marketlens.auth.dto.RefreshRequest;
import com.marketlens.auth.dto.RegisterRequest;
import com.marketlens.common.error.ConflictException;
import com.marketlens.common.error.UnauthorizedException;
import com.marketlens.common.security.JwtService;
import com.marketlens.user.Role;
import com.marketlens.user.UserAccount;
import com.marketlens.user.UserAccountRepository;
import com.marketlens.user.UserSummary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByUsername(request.username())) {
            throw new ConflictException("That username is already taken");
        }
        UserAccount user = new UserAccount(
                request.username(),
                request.displayName(),
                passwordEncoder.encode(request.password()),
                Set.of(Role.USER));
        users.save(user);
        return tokensFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = users.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Wrong username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Wrong username or password");
        }
        return tokensFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        UUID userId = jwtService.parseRefreshSubject(request.refreshToken());
        UserAccount user = users.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Account no longer exists"));
        return tokensFor(user);
    }

    @Transactional(readOnly = true)
    public UserSummary currentUser(UUID userId) {
        return UserSummary.from(users.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Account no longer exists")));
    }

    private AuthResponse tokensFor(UserAccount user) {
        return new AuthResponse(
                jwtService.createAccessToken(user),
                jwtService.createRefreshToken(user),
                UserSummary.from(user));
    }
}
