package com.barogo.delivery.api.config.error;

import com.barogo.delivery.response.ApiResponse;
import com.barogo.delivery.response.exception.BusinessException;
import com.barogo.delivery.response.exception.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class })
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("BadRequest", extractMessage(ex)));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        ErrorCode code = ex.getError();
        HttpStatus status = code.getHttpStatus();
        return ResponseEntity.status(status)
                .body(ApiResponse.error(code.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleEtc(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("InternalServerError", ex.getLocalizedMessage()));
    }

    private String extractMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException && methodArgumentNotValidException.getBindingResult().hasErrors()) {
            return methodArgumentNotValidException.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        } else if (ex instanceof BindException be && be.getBindingResult().hasErrors()) {
            return be.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        } else if (ex instanceof ConstraintViolationException cve && !cve.getConstraintViolations().isEmpty()) {
            return cve.getConstraintViolations().iterator().next().getMessage();
        }
        return "요청 값이 올바르지 않습니다.";
    }

}
