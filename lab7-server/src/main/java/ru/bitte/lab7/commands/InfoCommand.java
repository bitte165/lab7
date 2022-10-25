package ru.bitte.lab7.commands;

import ru.bitte.lab7.requests.ServerResponse;
import ru.bitte.lab7.route.Route;
import ru.bitte.lab7.server.CollectionKeeper;

import java.util.Set;

/**
 * An object of this class is used in {@code Terminal} as a command that outputs information about the current state
 * of the maintained collection. The information includes the type of the collection's elements, the creation date, the
 * current number of elements in it, the maximum and the minimum distances among the elements in the collection.
 * The object of this class is used by running the {@code run()} method.
 * @implNote A no-argument command
 */
public class InfoCommand extends Command {
    private final CollectionKeeper collection;

    /**
     * Constructs a {@code InfoCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public InfoCommand(CollectionKeeper collection) {
        super("info", "output information about the collection");
        this.collection = collection;
    }

    @Override
    public ServerResponse run() {
        StringBuilder output = new StringBuilder();
        Set<Route> elements = collection.copySorted();
        output.append("Information about this collection:\n");
        output.append("Type: ").append(collection.getCollectionType()).append("\n");
        output.append("Creation date: ").append(collection.getCreationDate().toString()).append("\n");
        output.append("Number of elements: ").append(collection.getCollectionSize()).append("\n");
        var min = elements.stream().min(Route::compareTo);
        var max = elements.stream().max(Route::compareTo);
        output.append("Max distance: ").append(max.isPresent() ? max.get() : "empty").append("\n");
        output.append("Min distance: ").append(min.isPresent() ? min.get() : "empty").append("\n");
        return new ServerResponse(output.toString().strip(), false);
    }
}
