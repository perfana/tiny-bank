package io.perfana.tinybank.service;

import org.springframework.http.HttpStatusCode;

public class ClientErrorException extends RuntimeException {
    private final HttpStatusCode statusCode;

    public ClientErrorException(HttpStatusCode statusCode, String statusText) {
        super("Client error: " + statusCode + " - " + statusText);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
