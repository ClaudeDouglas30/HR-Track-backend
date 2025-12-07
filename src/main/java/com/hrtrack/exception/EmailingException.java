package com.hrtrack.exception;

public class EmailingException extends Exception {
    public EmailingException(String message) {
        super(message);
    }

    public EmailingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailingException(Throwable cause) {
        super(cause);
    }

    public EmailingException() {}
}
