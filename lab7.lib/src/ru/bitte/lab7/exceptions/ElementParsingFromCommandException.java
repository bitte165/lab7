package ru.bitte.lab7.exceptions;

/**
 * A {@code ElementParsingFromCommandException} is thrown when element parsing from a command prompt has gone wrong.
 */
public class ElementParsingFromCommandException extends ElementParsingException {
    /**
     * Report a {@code ElementParsingFromCommandException} for the reason specified.
     * @param message a {@code String} message indicating what has gone wrong during an element parsing
     */
    public ElementParsingFromCommandException(String message) {
        super(message);
    }
}
