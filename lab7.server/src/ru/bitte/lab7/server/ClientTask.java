package ru.bitte.lab7.server;

import ru.bitte.lab7.requests.Request;
import ru.bitte.lab7.requests.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ClientTask implements Runnable {
    private User user;
    private final CommandHandler commandHandler;
    private final Socket connection;
    private final ExecutorService commandPool;
    private final ExecutorService responsePool;


    public ClientTask(CommandHandler ch, Socket conn, ExecutorService commandPool, ExecutorService responsePool) {
        commandHandler = ch;
        connection = conn;
        this.commandPool = commandPool;
        this.responsePool = responsePool;
    }

    @Override
    public void run() {
        try {
            InputStream in = connection.getInputStream();
            OutputStream out = connection.getOutputStream();
            while (true) {
                int header = ByteBuffer.wrap(in.readNBytes(4)).flip().getInt();
                byte[] body = in.readNBytes(header);
                Request request = (Request) Server.bytesToObject(body);
                Future<ResponseTask> response = commandPool.submit(new CommandTask(request, commandHandler, connection));
                ResponseTask responseTask = response.get();
                Future<Boolean> result = responsePool.submit(responseTask);
                if (result.get()) { // check if terminating
                    break;
                }
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
