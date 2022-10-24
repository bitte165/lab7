package ru.bitte.lab7.exceptions;

/**
 * An abstract exception class for the element parsing exceptions.
 */
public abstract class ElementParsingException extends Exception {
    /**
     * An abstract constructor.
     * @param message message for the abstract constructor
     */
    public ElementParsingException(String message) {
        super(message);
    }
}
