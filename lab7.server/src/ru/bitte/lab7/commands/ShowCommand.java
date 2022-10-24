package ru.bitte.lab7.commands;

import ru.bitte.lab7.requests.ServerResponse;
import ru.bitte.lab7.route.Route;
import ru.bitte.lab7.server.CollectionKeeper;

import java.util.Comparator;

/**
 * An object of this class is used in {@code Terminal} as a command that outputs all the elements in the maintained
 * representation in the standard string representation. The object of this class is used by running
 * the {@code run()} method.
 * @implNote A no-argument command
 */
public class ShowCommand extends Command {
    private final CollectionKeeper collection;

    /**
     * Constructs a {@code ShowCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public ShowCommand(CollectionKeeper collection) {
        super("show", "output all of the collection elements in the string representation");
        this.collection = collection;
    }

    @Override
    public ServerResponse run() {
        StringBuilder output = new StringBuilder();
        collection.copyCollection().stream().sorted(Comparator.comparing(Route::getName)).forEach(x -> output.append(x).append("\n"));
        return new ServerResponse(output.toString().strip(), false);
    }
}
