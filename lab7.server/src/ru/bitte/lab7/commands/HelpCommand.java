package ru.bitte.lab7.commands;

import ru.bitte.lab7.requests.ServerResponse;

import java.util.*;


/**
 * An object of this class is used in {@code Terminal} as a command that outputs the list of available commands and
 * their descriptions. The object of this class is used by running the {@code run()} method.
 * @implNote A no-argument command
 */
public class HelpCommand extends Command {
    private final Map<String, Command> commands;

    /**
     * Constructs a {@code HelpCommand} object.
     * @param commands the reference to a {@link HashMap} holding the commands
     */
    public HelpCommand(Map<String, Command> commands) {
        super("help", "list all available commands");
        this.commands = commands;
    }

    @Override
    public ServerResponse run() {
        StringBuilder output = new StringBuilder();
        List<Command> commandsObjects = new ArrayList<>(commands.values());
        commandsObjects.sort(Comparator.comparing(Command::getName));
        output.append("Available commands:\n");
        Iterator<Command> iterator = commandsObjects.iterator();
        while (iterator.hasNext()) {
            Command command = iterator.next();
            output.append(" - ").append(command.getName());
            if (command instanceof ArgumentCommand) { // appends the argument name if it's an argument command
                output.append(" ").append(((ArgumentCommand) command).getArgumentName());
            } else if (command instanceof IDCommand) { // appends the word "id" if it's an id command
                output.append(" id");
            }
            if (command instanceof ElementCommand) { // appends "{element}" if it's an element command
                output.append(" {element}");
            }
            // prints a period after the last command
            if (!iterator.hasNext()) {
                output.append(" : ").append(command.getDescription()).append(".\n");
            } else {
                output.append(" : ").append(command.getDescription()).append(",\n");
            }
        }
        output.append("Note: the \"{element}\" commands should not be provided an element argument. Instead, " +
                "an element constructor is called.");
        return new ServerResponse(output.toString(), false);
    }
}
