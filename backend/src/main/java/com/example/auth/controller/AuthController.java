package com.example.auth.controller;

import com.example.auth.model.AuthStatusResponse;
import com.example.auth.model.LoginRequest;
import com.example.auth.model.MfaRequest;
import com.example.auth.service.UserAuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserAuthService userAuthService;

    public AuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthStatusResponse> login(@RequestBody @Valid LoginRequest request, HttpSession session) {
        if (!userAuthService.passwordValid(request.username(), request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthStatusResponse(false, null, "Invalid username/password"));
        }
        session.setAttribute("PENDING_MFA", request.username());
        return ResponseEntity.ok(new AuthStatusResponse(false, request.username(), "Password accepted. MFA required."));
    }

    @PostMapping("/auth/mfa/verify")
    public ResponseEntity<AuthStatusResponse> verifyMfa(@RequestBody @Valid MfaRequest request, HttpSession session) {
        Object pendingUser = session.getAttribute("PENDING_MFA");
        if (pendingUser == null || !pendingUser.equals(request.username())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthStatusResponse(false, null, "Start login first."));
        }

        if (!userAuthService.verifyTotp(request.username(), request.code())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthStatusResponse(false, request.username(), "Invalid MFA code"));
        }

        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
            request.username(),
            null,
            java.util.List.of(() -> "ROLE_USER")
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        session.removeAttribute("PENDING_MFA");
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return ResponseEntity.ok(new AuthStatusResponse(true, request.username(), "MFA verified. Logged in."));
    }

    @GetMapping("/me")
    public AuthStatusResponse me(Principal principal) {
        if (principal == null) {
            return new AuthStatusResponse(false, null, "Anonymous");
        }
        return new AuthStatusResponse(true, principal.getName(), "Authenticated");
    }

    @PostMapping("/auth/logout")
    public AuthStatusResponse logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
        return new AuthStatusResponse(false, null, "Logged out");
    }

    @GetMapping("/auth/demo-mfa-secret")
    public String demoSecret() {
        return userAuthService.getDemoSecretBase64();
    }
}
