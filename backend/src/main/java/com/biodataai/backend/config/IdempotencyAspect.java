package com.biodataai.backend.config;

import com.biodataai.backend.annotation.Idempotent;
import com.biodataai.backend.service.IdempotencyService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP aspect that enforces idempotency-key requirement on @Idempotent endpoints.
 * Returns 409 Conflict if the key is a duplicate (already processed).
 */
@Aspect
@Component
public class IdempotencyAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyAspect.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final IdempotencyService idempotencyService;

    public IdempotencyAspect(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    @Around("@annotation(idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Missing required header: " + IDEMPOTENCY_KEY_HEADER);
        }

        // Extract userId from request attribute (set by FirebaseAuthFilter)
        UUID userId = (UUID) request.getAttribute(FirebaseAuthFilter.USER_ID_ATTRIBUTE);
        String endpoint = request.getRequestURI();

        try {
            idempotencyService.recordKey(idempotencyKey, userId, endpoint);
        } catch (IllegalArgumentException e) {
            log.warn("Duplicate idempotency key for endpoint {}: {}", endpoint, idempotencyKey);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate request (idempotency key)");
        }

        return joinPoint.proceed();
    }
}
