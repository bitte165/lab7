package ru.bitte.lab7.commands;

/**
 * An abstract class that extends the abstract class {@link Command} used for implementing commands that need a
 * {@code String} argument to run. It provides getter and setter methods used for passing a string when preparing the
 * command to run and getting it in the implementation of the {@code run()} method.
 */
public abstract class ArgumentCommand extends Command {
    private String argument;
    private final String argumentName;

    /**
     * Initializes the fields with the provided parameters.
     * @param name the name of the command
     * @param argumentName the name of the command's argument
     * @param desc the command description
     */
    public ArgumentCommand(String name, String argumentName, String desc) {
        super(name, desc);
        this.argumentName = argumentName;
    }

    /**
     * Returns the name of the command's argument
     * @return {@code String} representing the argument's name
     */
    public String getArgumentName() {
        return argumentName;
    }

    /**
     * Returns the current argument of the command
     * @return {@code String} representing the argument's value
     */
    public String getArgument() {
        return argument;
    }

    /**
     * Sets the argument with which the command will run the next time
     * @param argument {@code String} presenting the argument's value
     */
    public void passArgument(String argument) {
        this.argument = argument;
    }
}
