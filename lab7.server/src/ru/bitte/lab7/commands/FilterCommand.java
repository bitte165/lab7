package ru.bitte.lab7.commands;

import ru.bitte.lab7.requests.ServerResponse;
import ru.bitte.lab7.route.Route;
import ru.bitte.lab7.server.CollectionKeeper;

import java.util.List;
import java.util.Objects;

/**
 * An object of this class is used in {@code Terminal} as a command that filters the elements of the collection by their
 * names and prints the filtered elements' formatted representations. The object of this class is used by supplying the
 * string that must be contained in the filtered elements' names through the setter method of the parent class
 * {@link ArgumentCommand} and running by the {@code run()} method.
 * @see ArgumentCommand#passArgument(String)
 * @implNote An argument command
 */
public class FilterCommand extends ArgumentCommand {
    private final CollectionKeeper collection;

    /**
     * Constructs a {@code FilterCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public FilterCommand(CollectionKeeper collection) {
        super("filter_contains_name", "name", "output elements the names of which " +
                "contain a given substring");
        this.collection = collection;
    }

    @Override
    public ServerResponse run() {
        StringBuilder output = new StringBuilder();
        System.out.printf("Elements in the collection containing \"%s\" in the name:\n", getArgument());
        List<Route> filteredRoutes = collection.filterByString(Objects.requireNonNull(getArgument()));
        filteredRoutes.stream().map(Route::format).forEach((x) -> output.append(x).append("\n"));
        return new ServerResponse(output.toString().strip(), false);
    }
}
