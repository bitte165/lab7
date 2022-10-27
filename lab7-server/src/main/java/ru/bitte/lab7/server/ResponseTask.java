package ru.bitte.lab7.server;

import lombok.extern.log4j.Log4j;
import ru.bitte.lab7.requests.ServerResponse;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

@Log4j
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
            var out = client.getOutputStream();
            byte[] body = Server.objectToBytes(serverResponse);
            byte[] header = ByteBuffer.allocate(4).putInt(body.length).array();
            out.write(header);
            out.write(body);
            return serverResponse.isTerminating();
        } catch (IOException e) {
            log.error("Unknown IO exception occurred while working with a client");
            log.error("Exception description: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
