package ru.bitte.lab7.exceptions;

public class UserUnauthorizedException extends Exception {
    public UserUnauthorizedException(String message) {
        super(message);
    }
}
