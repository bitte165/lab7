package ru.bitte.lab7.commands;

import ru.bitte.lab7.requests.ServerResponse;

/**
 * An abstract class from which every single command implementation and other abstract command interfaces inherit.
 * This class provides the interface for the {@code run()} method and defines the initialization and use of
 * the {@code name} and {@code description} fields.
 */
public abstract class Command {
    private final String name;
    private final String description;

    /**
     * Initializes the fields with the provided parameters.
     * @param name the name of the command
     * @param description the command description
     */
    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Runs the command.
     */
    public abstract ServerResponse run();

    /**
     * Returns the name of the command
     * @return {@code String} representing the name of the command
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the command
     * @return {@code String} representing the description of the command
     */
    public String getDescription() {
        return description;
    }
}
