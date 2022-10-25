package ru.bitte.lab7.commands;

import ru.bitte.lab7.route.Route;

/**
 * An abstract class that extends the abstract class {@link Command} used for implementing commands that need a
 * {@link Route} object to run. It provides getter and setter methods used for passing a {@code Route} when preparing
 * the command to run and getting it in the implementation of the {@code run()} method. The command user is prompted
 * with a {@code Route} creation dialog that is called during the parsing of the command in terminal.
 */
public abstract class ElementCommand extends Command {
    private Route element;

    /**
     * Initializes the fields with the provided parameters.
     * @param name the name of the command
     * @param desc the command description
     */
    public ElementCommand(String name, String desc) {
        super(name, desc);
    }

    /**
     * Returns the current {@code Route} object passed to the command
     * @return {@code Route} object passed to the command
     */
    public Route getElement() {
        return element;
    }

    /**
     * Sets the {@code Route} object with which the command will run the next time
     * @param element {@code Route} object passed to the command
     */
    public void passElement(Route element) {
        this.element = element;
    }
}
