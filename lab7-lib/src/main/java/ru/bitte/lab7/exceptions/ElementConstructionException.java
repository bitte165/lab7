package ru.bitte.lab7.exceptions;

/**
 * A {@code ElementConstructionException} is thrown when a construction of an object
 * from the {@link ru.bitte.lab7.route} package has gone wrong.
 */
public class ElementConstructionException extends ElementException {
    /**
     * Report a {@code ElementConstructionException} for the reason specified.
     * @param message a {@code String} message indicating what has gone wrong during an object creation
     */
    public ElementConstructionException(String message) {
        super(message);
    }
}
