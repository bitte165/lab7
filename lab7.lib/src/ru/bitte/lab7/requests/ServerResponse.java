package ru.bitte.lab7.requests;

public class ServerResponse {
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
