package com.example.personalfinancebackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // Báo cho Spring biết đây là class xử lý lỗi toàn cục
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi Validation (khi @Valid thất bại).
     * Sẽ được kích hoạt bởi MethodArgumentNotValidException.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        // Tạo một Map để chứa (tên_trường, thông_báo_lỗi)
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu đầu vào không hợp lệ",
                fieldErrors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý các lỗi Nghiệp vụ (Business Logic) mà chúng ta tự ném ra.
     * (Ví dụ: "Số dư ví không đủ", "Không tìm thấy giao dịch", "Không có quyền"...)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessLogicException(
            RuntimeException ex, WebRequest request) {

        String message = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST; // Mặc định là 400

        // Phân loại lỗi dựa trên thông báo để trả về HTTP Status chính xác
        if (message.contains("Không tìm thấy")) {
            status = HttpStatus.NOT_FOUND; // 404
        } else if (message.contains("Không có quyền")) {
            status = HttpStatus.FORBIDDEN; // 403
        } else if (message.contains("Số dư ví không đủ") || message.contains("không hợp lệ")) {
            status = HttpStatus.BAD_REQUEST; // 400
        }
        // (Bạn có thể thêm các else if khác ở đây)

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(status.value(), message);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Xử lý tất cả các lỗi khác (lỗi 500 Internal Server Error)
     * (Ví dụ: NullPointerException, lỗi CSDL không lường trước...)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(
            Exception ex, WebRequest request) {

        // Log lỗi này ra (rất quan trọng)
        ex.printStackTrace();

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Đã có lỗi xảy ra ở máy chủ. Vui lòng thử lại sau."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}