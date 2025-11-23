package com.example.personalfinancebackend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
// Chỉ bao gồm các trường không-null
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {

    private int status;
    private String message;
    private long timestamp;
    private Map<String, String> errors; // Dùng cho lỗi validation

    // Constructor cho lỗi nghiệp vụ chung
    public ErrorResponseDTO(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now().toEpochMilli();
    }

    // Constructor cho lỗi validation
    public ErrorResponseDTO(int status, String message, Map<String, String> errors) {
        this(status, message); // Gọi constructor ở trên
        this.errors = errors;
    }
}