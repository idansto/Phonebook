package com.idan.phonebook.application.exceptions;

public class DuplicateContactException extends RuntimeException {
    public DuplicateContactException(String message, Exception cause){
        super(message, cause);
    }
}
