package io.perfana.tinybank.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ServerErrorException extends RuntimeException {
    private final HttpStatusCode statusCode;

    public ServerErrorException(HttpStatusCode statusCode, String statusText) {
        super("Server error: " + statusCode + " - " + statusText);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
