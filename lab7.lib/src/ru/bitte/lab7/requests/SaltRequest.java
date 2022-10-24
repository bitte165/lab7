package ru.bitte.lab7.requests;

public class SaltRequest implements Request {
    private String username;


    public SaltRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
