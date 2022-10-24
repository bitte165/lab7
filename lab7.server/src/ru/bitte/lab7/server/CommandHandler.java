package ru.bitte.lab7.server;

import ru.bitte.lab7.commands.*;
import ru.bitte.lab7.requests.*;
import ru.bitte.lab7.exceptions.ElementException;
import ru.bitte.lab7.commands.*;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class CommandHandler {
    private Map<String, Command> commands;
    private Queue<String> history;
    private User user;

    public CommandHandler(CollectionKeeper ck, User usr) {
        history = new ArrayBlockingQueue<>(15);
        commands = new HashMap<>();
        Set<Command> tempComs = new HashSet<>();
        tempComs.add(new AddCommand(ck));
        tempComs.add(new AddIfMinCommand(ck));
        tempComs.add(new ClearCommand(ck));
        tempComs.add(new FilterCommand(ck));
        tempComs.add(new HelpCommand(commands));
        tempComs.add(new HistoryCommand(history));
        tempComs.add(new InfoCommand(ck));
        tempComs.add(new PrintAscendingCommand(ck));
        tempComs.add(new PrintUniqueCommand(ck));
        tempComs.add(new RemoveByIDCommand(ck));
        tempComs.add(new RemoveGreaterCommand(ck));
        tempComs.add(new ShowCommand(ck));
        tempComs.add(new UpdateCommand(ck));
        tempComs.forEach(command -> commands.put(command.getName(), command));
    }

    public ServerResponse runCommand(Request cr) {
        AbstractCommandRequest commandRequest = (AbstractCommandRequest) cr;
        try {
            Command command = commands.get(commandRequest.getCommandName());
            if (!user.equals(commandRequest.getCredentials())) {
                throw new SecurityException("User credentials changed after authorization while running the app");
            }
            if (command instanceof ArgumentCommand) {
                ((ArgumentCommand) command).passArgument(((ArgumentCommandRequest) commandRequest).getArgument());
            }
            if (command instanceof IDCommand) {
                ((IDCommand) command).passID(Integer.parseInt(((ArgumentCommandRequest) commandRequest).getArgument()));
            }
            if (command instanceof ElementCommand) {
                ((ElementCommand) command).passElement(commandRequest.getElement().build());
            }
            if (command instanceof WriteCommand) {
                ((WriteCommand) command).setCredentials(commandRequest.getCredentials());
            }
            ServerResponse clientResponse = command.run();
            addToHistory(command.getName());
//            logger.info(String.format("Command \"%s\" was executed successfully", command.getName()));
            return clientResponse;
        } catch (ElementException e) {
//            logger.warning(String.format("Error with element \"%s\" while executing the command", e.getMessage()));
//            pendingResponses.put(key, "Element exception: " + e.getMessage());
            return new ServerResponse("Element exception: " + e.getMessage(), false);
        }
    }

    private void addToHistory(String commandName) {
        boolean firstTry = history.offer(commandName);
        if (!firstTry) {
            history.poll();
            history.offer(commandName);
        }
    }
}
