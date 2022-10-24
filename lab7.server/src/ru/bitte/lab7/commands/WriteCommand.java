package ru.bitte.lab7.commands;

import ru.bitte.lab7.requests.User;

public interface WriteCommand {
    void setCredentials(User credentials);

    User getCredentials();
}
