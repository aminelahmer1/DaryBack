package com.example.dari_back.exception;

public class MFAServerAppException extends RuntimeException{
    public MFAServerAppException(String message) {
        super(message);
    }

    public MFAServerAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
