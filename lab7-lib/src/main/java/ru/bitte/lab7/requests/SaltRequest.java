package ru.bitte.lab7.requests;

import java.io.Serializable;

public class SaltRequest implements Request, Serializable {
    private String username;


    public SaltRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
