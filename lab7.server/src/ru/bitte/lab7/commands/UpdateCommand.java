package ru.bitte.lab7.commands;

import ru.bitte.lab7.exceptions.ElementConstructionException;
import ru.bitte.lab7.exceptions.GetByIDException;
import ru.bitte.lab7.requests.ServerResponse;
import ru.bitte.lab7.requests.User;
import ru.bitte.lab7.route.Route;
import ru.bitte.lab7.server.CollectionKeeper;

// old ahh comments
/**
 * An object of this class is used in {@code Terminal} as a command that updates an element by the provided ID from the
 * maintained collection by replacing the fields provided by the passed element. As such, the ID and creation date are
 * left unmodified, only the new field values are replaced. The object of this class is used by supplying an ID of
 * an element from the collection through the {@code passID(int)} method, a {@code Route} object through the method of
 * the parent {@link ElementCommand} class and then running by the {@code run()} method.
 * @see ElementCommand#passElement(Route)
 * @implNote An ID-element command
 */
public class UpdateCommand extends ElementCommand implements IDCommand, WriteCommand {
    private final CollectionKeeper collection;
    private int id;
    private User credentials;

    /**
     * Constructs a {@code UpdateCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public UpdateCommand(CollectionKeeper collection) {
        super("update", "update the values of the collection element provided by the ID");
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
        Route varElement;
        // tries to retrieve an object by the id and if fails
        try {
            // get element by id, if no element is found an exception is called
            varElement = collection.getByID(id);
            Route newFields = getElement();
            assert varElement != null;
            // modifying the retrieved element with new fields if needed
            if (!varElement.getName().equals(newFields.getName())) {
                varElement = varElement.changeName(newFields.getName());
            }
            if (!varElement.getCoordinates().equals(newFields.getCoordinates())) {
                varElement = varElement.changeCoordinates(newFields.getCoordinates());
            }
            if (!varElement.getFrom().equals(newFields.getFrom())) {
                varElement = varElement.changeFrom(newFields.getFrom());
            }
            if (!varElement.getTo().equals(newFields.getTo())) {
                varElement = varElement.changeTo(newFields.getTo());
            }
            // replacing with the new modified object
            collection.replaceByID(varElement, getCredentials());
            output.append("The element by ID ").append(getID()).append(" has been updated successfully.");
        } catch (GetByIDException e){
            output.append("No element with such an ID found.");
        } catch (ElementConstructionException e){
            output.append("Couldn't change the object because: " + e.getMessage() + "\n" +
                    "The element by the provided ID was left intact.");
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
