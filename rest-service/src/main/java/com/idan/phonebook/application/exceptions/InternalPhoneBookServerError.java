package com.idan.phonebook.application.exceptions;

public class InternalPhoneBookServerError extends RuntimeException {
    public InternalPhoneBookServerError(String message){
        super(message);
    }
}
