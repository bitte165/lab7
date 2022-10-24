package ru.bitte.lab7.server;

import ru.bitte.lab7.requests.ServerResponse;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

public class ResponseTask implements Callable<Boolean> {
    private final ServerResponse serverResponse;
    private final Socket client;

    public ResponseTask(ServerResponse sr, Socket c) {
        serverResponse = sr;
        client = c;
    }

    @Override
    public Boolean call() {
        try {
            byte[] body = Server.objectToBytes(serverResponse);
            byte[] header = ByteBuffer.allocate(4).putInt(body.length).flip().array();
            var out = client.getOutputStream();
            out.write(header);
            out.write(body);
            return serverResponse.isTerminating();
            // log
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
