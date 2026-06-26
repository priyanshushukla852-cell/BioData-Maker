package com.biodataai.backend.config;

import com.biodataai.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Verifies the {@code Authorization: Bearer <firebaseIdToken>} header on authenticated routes
 * and stores the resolved internal user id as a request attribute ({@link #USER_ID_ATTRIBUTE}),
 * which controllers read via {@code @RequestAttribute}.
 */
@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    public static final String USER_ID_ATTRIBUTE = "userId";

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public FirebaseAuthFilter(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/api/biodatas") || path.startsWith("/api/ai"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Missing bearer token.");
            return;
        }

        String idToken = header.substring("Bearer ".length());
        try {
            var token = authService.verifyIdToken(idToken);
            var result = authService.upsertUser(token);
            request.setAttribute(USER_ID_ATTRIBUTE, result.user().getId());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            HttpStatus status = e instanceof com.biodataai.backend.exception.ApiException apiException
                    ? apiException.getStatus()
                    : HttpStatus.UNAUTHORIZED;
            writeError(response, status, e.getMessage());
        }
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of("error", message));
    }
}
