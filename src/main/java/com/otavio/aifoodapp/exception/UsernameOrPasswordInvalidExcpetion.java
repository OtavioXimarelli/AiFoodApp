package com.otavio.aifoodapp.exception;

public class UsernameOrPasswordInvalidExcpetion extends RuntimeException {
    public UsernameOrPasswordInvalidExcpetion(String message) {
        super(message);
    }

    public UsernameOrPasswordInvalidExcpetion(String message, Throwable cause) {
        super(message, cause);
    }
}
