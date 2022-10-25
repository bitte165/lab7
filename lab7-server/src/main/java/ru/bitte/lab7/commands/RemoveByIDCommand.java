package ru.bitte.lab7.commands;

import lombok.extern.log4j.Log4j;
import ru.bitte.lab7.exceptions.GetByIDException;
import ru.bitte.lab7.exceptions.UserUnauthorizedException;
import ru.bitte.lab7.requests.ServerResponse;
import ru.bitte.lab7.requests.User;
import ru.bitte.lab7.route.Route;
import ru.bitte.lab7.server.CollectionKeeper;

import java.sql.SQLException;

/**
 * An object of this class is used in {@code Terminal} as a command that removes an object with the provided ID from the
 * maintained collection. The object of this class is used by supplying an ID of a {@link Route} object through the
 * {@code passID(int)} method and then running by the {@code run()} method.
 * @implNote An ID command
 */
@Log4j
public class RemoveByIDCommand extends Command implements IDCommand, WriteCommand {
    private final CollectionKeeper collection;
    private int id;
    private User credentials;

    /**
     * Constructs a {@code RemoveByIDCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public RemoveByIDCommand(CollectionKeeper collection) {
        super("remove_by_id", "remove an element of the specified ID from the collection");
        this.collection = collection;
    }

    @Override
    public void passID(int id) {
        this.id = id;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public ServerResponse run() {
        StringBuilder output = new StringBuilder();
        try {
            collection.removeByID(id, getCredentials());
            output.append("Successfully removed the element by ID.").append(getID());
        } catch (GetByIDException e) {
            output.append("No element with such an ID found.");
        } catch (UserUnauthorizedException e) {
            output.append("Couldn't remove the element because you don't have rights to it");
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
