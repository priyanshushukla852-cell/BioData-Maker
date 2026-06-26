package com.biodataai.backend.exception;

import org.springframework.http.HttpStatus;

public class AiServiceException extends ApiException {
    public AiServiceException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
