package ru.bitte.lab7.exceptions;

public class ClientDisconnectedException extends Exception {
    public ClientDisconnectedException(String message) {
        super(message);
    }
}
