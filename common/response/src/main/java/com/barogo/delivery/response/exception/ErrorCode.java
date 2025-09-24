package com.barogo.delivery.response.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    /* 400 BAD_REQUEST */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BadRequest"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "ValidationError"),
    ILLEGAL_STATUS(HttpStatus.BAD_REQUEST, "StatusError"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "InvalidParameter"),

    /* 401 UNAUTHORIZED */
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "MissingAccessToken"),
    UNAUTHORIZED_ERROR(HttpStatus.UNAUTHORIZED, "UnauthorizedError"),

    /* 403 FORBIDDEN */
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),

    /* 404 NOT_FOUND */
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ResourceNotFound"),

    /* 409 CONFLICT */
    DUPLICATE_EXCEPTION(HttpStatus.CONFLICT, "DuplicateException"),

    /* 500 INTERNAL_SERVER_ERROR */
    ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "InternalServerError");

    private final HttpStatus httpStatus;
    private final String code;

    ErrorCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

}
