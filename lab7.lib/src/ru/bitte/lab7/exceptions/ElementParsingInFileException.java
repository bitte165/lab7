package ru.bitte.lab7.exceptions;

/**
 * A {@code ElementParsingInFileException} is thrown when element parsing from a file has gone wrong.
 */
public class ElementParsingInFileException extends ElementParsingException {
    /**
     * Report a {@code ElementParsingInFileException} for the reason specified.
     * @param message a {@code String} message indicating what has gone wrong during an element parsing
     */
    public ElementParsingInFileException(String message) {
        super(message);
    }
}
