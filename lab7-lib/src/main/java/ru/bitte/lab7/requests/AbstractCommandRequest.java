package ru.bitte.lab7.requests;

import ru.bitte.lab7.exceptions.ElementException;
import ru.bitte.lab7.route.Route;

import java.io.Serializable;
import java.util.Set;

public abstract class AbstractCommandRequest implements Request, Serializable {
    private final String name;
    private Route.RouteBuilder element;
    private User credentials;

    protected static final Set<String> elementCommands = Set.of("add", "update", "add_if_min", "remove_greater");

    public AbstractCommandRequest(String n) {
        name = n;
        element = null;
    }

    public String getCommandName() {
        return name;
    }

    public Route.RouteBuilder getElement() throws ElementException {
        if (element == null) {
            throw new ElementException("Can't get this command's element - it doesn't require one!");
        }
        return element;
    }

    protected void setElement(Route.RouteBuilder e) {
        element = e;
    }

    public void setCredentials(User credentials) {
        this.credentials = credentials;
    }

    public User getCredentials() {
        return credentials;
    }
}
