package com.biodataai.backend.controller;

import com.biodataai.backend.dto.VerifyTokenRequest;
import com.biodataai.backend.dto.VerifyTokenResponse;
import com.biodataai.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Verifies a Firebase ID token obtained client-side (Google or Phone OTP sign-in) and
 * upserts the corresponding {@code User} row. See CLAUDE.md / PRD §11 deviation note: Firebase
 * Phone Auth verifies the OTP client-side only, so the backend verifies the resulting ID token
 * rather than the raw OTP.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/verify-token")
    public VerifyTokenResponse verifyToken(@Valid @RequestBody VerifyTokenRequest request) {
        var token = authService.verifyIdToken(request.idToken());
        var result = authService.upsertUser(token);
        return new VerifyTokenResponse(result.user().getId(), result.isNewUser(), result.user().getDisplayName());
    }
}
