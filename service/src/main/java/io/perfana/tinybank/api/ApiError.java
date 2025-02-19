package io.perfana.tinybank.api;

public class ApiError {
    private String message;
    private String errorId;

    public ApiError(String message, String errorId) {
        this.message = message;
        this.errorId = errorId;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorId() {
        return errorId;
    }
}
