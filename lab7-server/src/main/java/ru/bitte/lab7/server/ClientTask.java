package ru.bitte.lab7.server;

import lombok.extern.log4j.Log4j;
import ru.bitte.lab7.requests.AbstractCommandRequest;
import ru.bitte.lab7.requests.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Log4j
public class ClientTask implements Runnable {
    private final CommandHandler commandHandler;
    private final Socket connection;
    private final User user;
    private final ExecutorService commandPool;
    private final ExecutorService responsePool;


    public ClientTask(CommandHandler ch, Socket conn, User user, ExecutorService commandPool, ExecutorService responsePool) {
        commandHandler = ch;
        connection = conn;
        this.user = user;
        this.commandPool = commandPool;
        this.responsePool = responsePool;
    }

    @Override
    public void run() {
        try {
            InputStream in = connection.getInputStream();
//            OutputStream out = connection.getOutputStream();
            while (true) {
                int header = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                byte[] body = in.readNBytes(header);
                AbstractCommandRequest request = (AbstractCommandRequest) Server.bytesToObject(body);
                if (!user.equals(request.getCredentials())) {
                    throw new SecurityException("User credentials changed after authorization while running the app");
                }
                log.info(String.format("Accepted command \"%s\" from the user \"%s\"", request.getCommandName(), user.getUsername()));
                Future<ResponseTask> response = commandPool.submit(new CommandTask(request, commandHandler, connection));
                log.info(String.format("Ran the \"%s\" command", request.getCommandName()));
                ResponseTask responseTask = response.get();
                Future<Boolean> result = responsePool.submit(responseTask);
                log.info(String.format("Send \"%s\" response to the command", user.getUsername()));
                if (result.get()) { // check if terminating
                    log.info(String.format("The user \"%s\" has successfully disconnected by the exit command",
                            request.getCredentials().getUsername()));
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Unknown IO exception occurred while working with a client");
            log.error("Exception description: " + e.getMessage());
//            throw new RuntimeException(e);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Unknown error while executing a task");
            log.error("Exception description: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
