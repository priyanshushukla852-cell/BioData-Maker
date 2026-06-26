package com.biodataai.backend.exception;

import org.springframework.http.HttpStatus;

public class AuthUnavailableException extends ApiException {
    public AuthUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
