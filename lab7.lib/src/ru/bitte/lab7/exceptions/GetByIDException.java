package ru.bitte.lab7.exceptions;

/**
 * A {@code GetByIDException} is thrown when an element by an ID couldn't be found in a collection.
 */
public class GetByIDException extends Exception {
    /**
     * Report a {@code GetByIDException} with a specified message.
     * @param message a {@code String} message reporting the exception
     */
    public GetByIDException(String message) {
        super(message);
    }
}
