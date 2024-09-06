package com.idan.phonebook.application.exceptions;

public class ContactNotExistsException extends RuntimeException {
    public ContactNotExistsException(String message){
        super(message);
    }
}
