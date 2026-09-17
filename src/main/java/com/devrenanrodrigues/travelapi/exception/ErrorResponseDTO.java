package com.devrenanrodrigues.travelapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ErrorResponseDTO of(Integer status, String error, String message, String path) {
        return new ErrorResponseDTO(Instant.now(), status, error, message, path, null);
    }

    public static ErrorResponseDTO of(Integer status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponseDTO(Instant.now(), status, error, message, path, fieldErrors);
    }
}
