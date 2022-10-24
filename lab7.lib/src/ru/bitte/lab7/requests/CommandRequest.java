package ru.bitte.lab7.requests;

import ru.bitte.lab7.exceptions.CommandParsingException;
import ru.bitte.lab7.exceptions.ElementConstructionException;
import ru.bitte.lab7.route.Coordinates;
import ru.bitte.lab7.route.Location;
import ru.bitte.lab7.route.Route;

import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandRequest extends AbstractCommandRequest {
    protected static final Set<String> nonArgCommands = Set.of("help", "info", "show", "add", "clear", "save", "exit",
            "add_if_min", "remove_greater", "history", "print_ascending", "print_unique_distance");

    public CommandRequest(String name) throws CommandParsingException, ElementConstructionException {
        super(name);
        if (ArgumentCommandRequest.argComamnds.contains(name)) {
            throw new CommandParsingException("Incorrect command usage. Please see \"help\" on command usage.");
        } else if (!nonArgCommands.contains(name)) {
            throw new CommandParsingException("Unknown command. Please see \"help\" for the list of commands.");
        }
        if (elementCommands.contains(name)) {
            setElement(generateElement());
        }
    }

    public static Route.RouteBuilder generateElement() throws ElementConstructionException {
        Route.RouteBuilder route = new Route.RouteBuilder();
        Scanner in = new Scanner(System.in);
        System.out.println("Assembling a Route object...");
        // get name
        System.out.print("Enter a name (can't be empty): ");
        String name = in.nextLine().strip();
        route.addName(name);
        // get coordinates
        Coordinates coords;
        Pattern coordPattern = Pattern.compile("^([\\d]+),[\\s]*([\\d]+)[^\\S\\r\\n]*$");
        System.out.println("Enter X and Y coordinates of the current position separated by a comma:");
        String coordInput = in.nextLine();
        Matcher coordMatch = coordPattern.matcher(coordInput);
        long coordX, coordY;
        if (coordMatch.matches()) {
            try {
                coordX = Long.parseLong(coordMatch.group(1));
                coordY = Long.parseLong(coordMatch.group(2));
            } catch (NumberFormatException e) {
                throw new ElementConstructionException("Incorrect values for coordinates");
            }
        } else {
            throw new ElementConstructionException("Error reading the coordinates");
        }
        coords = new Coordinates(coordX, coordY);
        route.addCoordinates(coords);
        Location from = generateLocation(in, "from");
        route.addFrom(from);
        Location to = generateLocation(in, "to");
        route.addTo(to);
        return route;
    }

    private static Location generateLocation(Scanner in, String kind) throws ElementConstructionException {
        Pattern locPattern = Pattern.compile("^([\\d]+),[\\s]*([\\d]+),[\\s]*([0-9]+(?:\\.[0-9]+)?)[^\\S\\r\\n]*$");
        System.out.printf("Enter X, Y and Z coordinates of the \"%s\" point separated by a comma:\n", kind);
        String locInput = in.nextLine();
        Matcher locMatch = locPattern.matcher(locInput);
        long x,y;
        float z;
        if (locMatch.matches()) {
            try {
                x = Long.parseLong(locMatch.group(1));
                y = Long.parseLong(locMatch.group(2));
                z = Float.parseFloat(locMatch.group(3));
            } catch (NumberFormatException e) {
                throw new ElementConstructionException("Incorrect values for location");
            }
        } else {
            throw new ElementConstructionException("Error reading the location");
        }
        System.out.println("Enter the name of that location (empty for the default)");
        String name = in.nextLine().strip();
        if (name.equals("")) {
            name = "Unnamed location";
        }
        return new Location(x, y, z, name);
    }

    // todo
//    private static Route.RouteBuilder() updateRoute()
}
