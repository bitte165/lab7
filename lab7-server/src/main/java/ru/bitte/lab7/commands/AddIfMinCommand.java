package ru.bitte.lab7.commands;

import lombok.extern.log4j.Log4j;
import ru.bitte.lab7.requests.ServerResponse;
import ru.bitte.lab7.requests.User;
import ru.bitte.lab7.route.Route;
import ru.bitte.lab7.server.CollectionKeeper;

import java.sql.SQLException;
import java.util.Collections;

/**
 * An object of this class is used in {@code Terminal} as a command that creates and adds a new {@link Route} object to
 * the maintained collection of objects if the object's distance value is less than the minimum distance value in the
 * collection. The object of this class is used by supplying a {@code Route} object through the method of the parent
 * {@link ElementCommand} class and then running by the {@code run()} method.
 * @see ElementCommand#passElement(Route)
 * @implNote An element command
 */
@Log4j
public class AddIfMinCommand extends ElementCommand implements WriteCommand {
    private final CollectionKeeper collection;
    private User credentials;

    /**
     * Constructs a {@code AddIfMinCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public AddIfMinCommand(CollectionKeeper collection) {
        super("add_if_min", "add a new element to the collection if its value is less than " +
                "that of the minimum element in the collection");
        this.collection = collection;
    }

    @Override
    public ServerResponse run() {
        StringBuilder output = new StringBuilder();
        Route min = Collections.min(collection.copySorted());
        if (getElement().getDistance() < min.getDistance()) {
            try {
                collection.addElement(getElement(), getCredentials());
                output.append(String.format("Added %s to the collection", getElement().getName()));
            } catch (SQLException e) {
                log.error("Unexpected database exception occurred during the command execution");
                log.debug(e.getMessage());
                output.append("Unexpected database exception occurred during the command execution");
//            throw new RuntimeException(e);
            }
        } else {
            output.append("\nDidn't add the element since its distance value was greater than " +
                    "the minimum one in the collection");
        }
        return new ServerResponse(output.toString().strip(), false);
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
