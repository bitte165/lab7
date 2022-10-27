package ru.bitte.lab7.commands;

import lombok.extern.log4j.Log4j;
import ru.bitte.lab7.exceptions.BatchRemovalException;
import ru.bitte.lab7.requests.ServerResponse;
import ru.bitte.lab7.requests.User;
import ru.bitte.lab7.server.CollectionKeeper;

import java.sql.SQLException;

/**
 * An object of this class is used in {@code Terminal} as a command that the elements of the collection the distance of
 * which is greater than the distance of the provided element. The object of this class is used by supplying a
 * {@code Route} object through the method of the parent {@link ElementCommand} class and then running by
 * the {@code run()} method.
 * @implNote An element command
 */
@Log4j

public class RemoveGreaterCommand extends ElementCommand implements WriteCommand {
    private final CollectionKeeper collection;
    private User credentials;

    /**
     * Constructs a {@code RemoveGreaterCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public RemoveGreaterCommand(CollectionKeeper collection) {
        super("remove_greater", "remove all of the collection elements " +
                "the distance of which exceeds the given element's");
        this.collection = collection;
    }

    @Override
    public ServerResponse run() {
        StringBuilder output = new StringBuilder();
//        Scanner in = new Scanner(System.in);
        // gets the elements the distance of which is greater than of the provided one
        try {
            collection.removeElementsGreaterThan(getElement(), getCredentials());
            output.append("Successfully removed elements without access errors");
        } catch (BatchRemovalException e) {
            output.append("Couldn't remove all elements requested because of access errors");
        } catch (SQLException e) {
            log.error("Unexpected database exception occurred during the command execution");
            log.debug(e.getMessage());
            output.append("Unexpected database exception occurred during the command execution");
//            throw new RuntimeException(e);
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

