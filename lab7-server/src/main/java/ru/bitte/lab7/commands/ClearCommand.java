package ru.bitte.lab7.commands;

import lombok.extern.log4j.Log4j;
import ru.bitte.lab7.exceptions.BatchRemovalException;
import ru.bitte.lab7.requests.ServerResponse;
import ru.bitte.lab7.requests.User;
import ru.bitte.lab7.server.CollectionKeeper;

import java.sql.SQLException;

/**
 * An object of this class is used in {@code Terminal} as a command that removes every element in the maintained
 * collection. Before clearing the collection, the used will be prompted with confirmation of clearing the collection.
 * The object of this class is used by running the {@code run()} method.
 * @implNote A no-argument command
 */
@Log4j
public class ClearCommand extends Command implements WriteCommand {
    private final CollectionKeeper collection;
    private User credentials;

    /**
     * Constructs a {@code ClearCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public ClearCommand(CollectionKeeper collection) {
        super("clear", "clear the collection");
        this.collection = collection;
    }

    @Override
    public ServerResponse run() {
        StringBuilder output = new StringBuilder();
        try {
            collection.clearCollection(getCredentials());
            output.append("Successfully cleared the collection");
        } catch (BatchRemovalException e) {
            output.append("Couldn't remove all the elements because of access errors");
        } catch (SQLException e) {
            log.error("Unexpected database exception occurred during the command execution");
            log.debug(e.getMessage());
            output.append("Unexpected database exception occurred during the command execution");
//            throw new RuntimeException(e);
        }
//        Scanner in = new Scanner(System.in);
//        output.append("Are you sure you want to do this? [Y/n]: ");
//        String response = in.nextLine();
//        if (response.equals("Y")) {
//
//            output.append("Successfully cleared the collection.");
//        } else {
//            output.append("Canceled the command.");
//        }
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
