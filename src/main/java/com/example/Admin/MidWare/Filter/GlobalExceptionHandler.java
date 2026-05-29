package com.example.Admin.MidWare.Filter;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Bắt lỗi CustomError do bạn tự throw
    @ExceptionHandler(CustomError.class)
    public ResponseEntity<Map<String, Object>> handleCustomError(CustomError ex, HttpServletRequest request) {
        logger.error("CustomError: {}", ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getError(), ex.getMessage(), request.getRequestURI());
    }

    // 2. Bắt lỗi ArgumentException / IllegalArgumentException (Bad Request)
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex, HttpServletRequest request) {
        logger.error("Bad Request Exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), request.getRequestURI());
    }

    // 3. Bắt lỗi Không tìm thấy phần tử (Not Found)
    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(Exception ex, HttpServletRequest request) {
        logger.warn("Not Found Exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
    }

    // 4. Bắt lỗi Xác thực / Quyền truy cập (Unauthorized)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(Exception ex, HttpServletRequest request) {
        logger.warn("Unauthorized Exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED.value(), "UnAuthorized", ex.getMessage(), request.getRequestURI());
    }

    // 5. Bắt lỗi Xung đột trạng thái / Thao tác không hợp lệ (Conflict)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(Exception ex, HttpServletRequest request) {
        logger.error("Invalid Operation Exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT.value(), "Invalid Operation Exception", ex.getMessage(), request.getRequestURI());
    }

    // 6. Các lỗi hệ thống không mong muốn khác (Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled Exception: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server", ex.getMessage(), request.getRequestURI());
    }

    // Hàm phụ trợ đóng gói JSON trả về giống định nghĩa ErrorRespone bên C# của bạn
    private ResponseEntity<Map<String, Object>> buildResponse(int status, String error, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("Error", error);
        body.put("message", message);
        body.put("path", path);
        return new ResponseEntity<>(body, HttpStatus.valueOf(status));
    }
}