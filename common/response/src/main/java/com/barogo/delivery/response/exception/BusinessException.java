package com.barogo.delivery.response.exception;

public class BusinessException extends RuntimeException {
    private final ErrorCode error;

    public BusinessException(ErrorCode error) {
        super(error.getCode());
        this.error = error;
    }

    public BusinessException(ErrorCode error, String message) {
        super(message);
        this.error = error;
    }

    public BusinessException(ErrorCode error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public BusinessException(ErrorCode error, Throwable cause) {
        super(error.getCode(), cause);
        this.error = error;
    }

    public ErrorCode getError() {
        return error;
    }
}

