package ru.bitte.lab7.commands;

import ru.bitte.lab7.requests.ServerResponse;

public class ExitCommand extends Command {

    public ExitCommand() {
        super("exit", "exit the program without saving the collection");
    }

    @Override
    public ServerResponse run() {
        return new ServerResponse("Exiting...", true);
    }
}
