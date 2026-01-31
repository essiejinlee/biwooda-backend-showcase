package com.example.biwooda.payment.exception;

public class AlreadyBorrowedException extends Exception {

    public AlreadyBorrowedException() {
        super("User already borrowed");
    }

    public AlreadyBorrowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyBorrowedException(Throwable cause) {
        super(cause);
    }
}
