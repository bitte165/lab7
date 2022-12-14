package ru.bitte.lab7.commands;

import lombok.extern.log4j.Log4j;
import ru.bitte.lab7.requests.ServerResponse;
import ru.bitte.lab7.requests.User;
import ru.bitte.lab7.route.Route;
import ru.bitte.lab7.server.CollectionKeeper;

import java.sql.SQLException;

/**
 * An object of this class is used in {@code Terminal} as a command that creates and adds a new {@link Route} object to
 * the maintained collection of objects. The object of this class is used by supplying a {@code Route} object through
 * the method of the parent {@link ElementCommand} class and then running by the {@code run()} method.
 * @see ElementCommand#passElement(Route)
 * @implNote An element command
 */
@Log4j
public class AddCommand extends ElementCommand implements WriteCommand {
    private final CollectionKeeper collection;
    private User credentials;

    /**
     * Constructs a {@code AddCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public AddCommand(CollectionKeeper collection) {
        super("add", "add a new element to the collection");
        this.collection = collection;
    }

    @Override
    public ServerResponse run() {
        StringBuilder output = new StringBuilder();
        try {
            collection.addElement(getElement(), getCredentials());
            output.append(String.format("Added %s to the collection", getElement().getName()));
        } catch (SQLException e) {
            log.error("Unexpected database exception occurred during the command execution");
            log.debug(e.getMessage());
            output.append("Unexpected database exception occurred during the command execution");
            throw new RuntimeException(e);
        }

        return new ServerResponse(output.toString(), false);
    }

    @Override
    public void setCredentials(User credentials) {
        this.credentials = credentials;
    }

    @Override
    public User getCredentials() {
        return credentials;
    }
}
