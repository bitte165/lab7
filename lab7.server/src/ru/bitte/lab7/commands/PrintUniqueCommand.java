package ru.bitte.lab7.commands;

import ru.bitte.lab7.requests.ServerResponse;
import ru.bitte.lab7.route.Route;
import ru.bitte.lab7.server.CollectionKeeper;

import java.util.stream.Collectors;

/**
 * An object of this class is used in {@code Terminal} as a command that prints all the unique distance values of the
 * elements of the maintained collection. The object of this class is used by running the {@code run()} method.
 * @implNote A no-argument command
 */
public class PrintUniqueCommand extends Command {
    private final CollectionKeeper collection;

    /**
     * Constructs a {@code PrintUniqueCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public PrintUniqueCommand(CollectionKeeper collection) {
        super("print_unique_distance", "output all the unique \"distance\" field values");
        this.collection = collection;
    }

    @Override
    public ServerResponse run() {
        // get the distance values, keep only distinct, sort and join with a comma
        return new ServerResponse(collection.copyCollection().stream().map(Route::getDistance)
                .distinct().sorted().map(x -> Integer.toString(x)).collect(Collectors.joining(", ")), false);
    }
}
