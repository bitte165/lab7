package ru.bitte.lab7.exceptions;

/**
 * A {@code CommandParsingException} is thrown when a command parsing has gone wrong.
 */
public class CommandParsingException extends Exception {
    /**
     * Report a {@code CommandParsingException} for the reason specified.
     * @param message a {@code String} message indicating what has gone wrong during a command parsing
     */
    public CommandParsingException(String message) {
        super(message);
    }
}
