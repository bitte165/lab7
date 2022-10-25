package ru.bitte.lab7.server;

import ru.bitte.lab7.requests.Request;
import ru.bitte.lab7.requests.ServerResponse;

import java.net.Socket;
import java.util.concurrent.Callable;

public class CommandTask implements Callable<ResponseTask> {
    private final Request request;
    private final CommandHandler commandHandler;
    private final Socket connection;

    public CommandTask(Request r, CommandHandler ch, Socket conn) {
        request = r;
        commandHandler = ch;
        connection = conn;
    }

    @Override
    public ResponseTask call() {
        ServerResponse result = commandHandler.runCommand(request);
        return new ResponseTask(result, connection);
    }
}
