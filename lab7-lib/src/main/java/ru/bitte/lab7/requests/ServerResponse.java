package ru.bitte.lab7.requests;

import java.io.Serializable;

public class ServerResponse implements Serializable {
    private final String response;
    private final boolean terminating;

    public ServerResponse(String resp, boolean term) {
        response = resp;
        terminating = term;
    }

    public String getResponse() {
        return response;
    }

    public boolean isTerminating() {
        return terminating;
    }
}
