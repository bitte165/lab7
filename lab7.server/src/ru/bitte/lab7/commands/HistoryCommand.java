package ru.bitte.lab7.commands;

import ru.bitte.lab7.requests.ServerResponse;

import java.util.Arrays;
import java.util.Queue;

/**
 * An object of this class is used in {@code Terminal} as a command that outputs the last 15 used commands in the current
 * terminal. The commands are printed without their arguments. The object of this class is used by running
 * the {@code run()} method.
 * @implNote A no-argument command
 */
public class HistoryCommand extends Command {
    private final Queue<String> history;

    /**
     * Constructs a {@code HistoryCommand} object.
     * @param history the list that maintains the latest run commands
     */
        public HistoryCommand(Queue<String> history) {
        super("history", "output the last 15 used commands (without their arguments)");
        this.history = history;
    }

    @Override
    public ServerResponse run() {
        StringBuilder output = new StringBuilder();
        System.out.println("Last 15 used commands:");
        Arrays.stream(history.toArray()).forEach((x) -> output.append(x).append("\n"));
        return new ServerResponse(output.toString(), false);
    }
}
