package ru.bitte.lab7.route;

import ru.bitte.lab7.exceptions.ElementConstructionException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;
import java.util.Random;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * A {@code Route} object represents a route from a starting point {@code from} to a destination point {@code to}.
 * A route also has a name, a unique ID, the creation date, distance calculated from the starting and destination
 * points, and the current coordinates en route, represented by a {@link Coordinates} object which has two values of
 * the {@code long} type, {@code X} and {@code Y}. {@code from} and {@code to} are objects of the class {@link Location},
 * which has the {@code X} and {@code Y} coordinates represented by {@code long}, a {@code Z} coordinate represented
 * by a {@code float} and a name represented by a {@link String}. The name must not be null nor empty. The ID field is
 * represented by an {@code int} and is generated automatically as a random integer that is then incremented with every
 * new object. The creation date, {@code creationDate}, is an object of the class {@link LocalDateTime}. The distance
 * is an object of the {@link Integer} class and is calculated as the length of a line segment between the two points
 * {@code from} and {@code to}. If the calculated distance is less than or equal to 1, then the object cannot be created
 * and a {@link ElementConstructionException} is thrown.
 *
 * @implNote This class is immutable and the values returned by {@code change} methods are that same object with a
 * particular field modified.
 */
public final class Route implements Comparable<Route> {
    private final Integer id; // can't be null, must be greater than 0, is generated automatically
    private final String name; // can't be null nor empty
    private final Coordinates coordinates; // can't be null
    private final LocalDateTime creationDate; // can't be null, is generated automatically
    private final Location from; // can't be null
    private final Location to; // can't be null
    private final Integer distance; // can't be null, must be greater than 1
    public static int nextId = new Random().nextInt(1000,9999);
    private String userCreated = null;

    /**
     * Constructs a {@code Route} with the passed down parameters, distance calculated from the starting and destination
     * points, creation time and a unique id.
     * @param name the name of the created object
     * @param coordinates the current position in the route
     * @param from the coordinates of the destination point
     * @param to the coordinates of the starting point
     * @throws ElementConstructionException if the calculated distance between the points is less than or equal to 1
     */
    public Route(String name, Coordinates coordinates, Location from, Location to) throws ElementConstructionException {
        this.id = nextId++;
//        this.id = id;
        this.creationDate = LocalDateTime.now();
//        this.id = abs(LocalDateTime.now().toString().hashCode());
        this.name = Objects.requireNonNull(name, "null name provided");
        if (name.length() == 0) throw new ElementConstructionException("Empty name string provided");
        this.coordinates = Objects.requireNonNull(coordinates, "null coordinates provided");
        this.from = Objects.requireNonNull(from, "null departure point provided");
        this.to = Objects.requireNonNull(to, "null destination point provided");
        distance = calculateDistance(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
        if (distance <= 1)
            throw new ElementConstructionException("the calculated distance turned out to be less than or equal to 1");
    }

    /* special constructor used for returning objects with changed fields
     (since Route is immutable and there needs to be a way to change some fields) */
    public Route(Integer id, String name, Coordinates coordinates, LocalDateTime creationDate,
                  Location from, Location to, Integer distance) throws ElementConstructionException {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        if (name.length() == 0) throw new IllegalArgumentException("Empty name string provided");
        this.coordinates = Objects.requireNonNull(coordinates);
        this.creationDate = creationDate;
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.distance = distance;
        if (distance <= 1)
            throw new ElementConstructionException("the calculated distance turned out to be less than or equal to 1");
    }

    /**
     * Returns a modified copy of this {@code Route} object with a changed name.
     * @param newName the new value of the {@code name} field
     * @return the {@code Route} object with a new name
     */
    public Route changeName(String newName) {
        try {
            return new Route(this.id, newName, this.coordinates, this.creationDate, this.from, this.to, this.distance);
        } catch (ElementConstructionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a modified copy of this {@code Route} object with changed coordinates.
     * @param newCoordinates the new value of the {@link Coordinates} field
     * @return the {@code Route} object with new coordinates
     */
    public Route changeCoordinates(Coordinates newCoordinates) {
        try {
            return new Route(this.id, this.name, newCoordinates, this.creationDate, this.from, this.to, this.distance);
        } catch (ElementConstructionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a modified copy of this {@code Route} object with a changed starting point.
     * <b>Warning</b>: As a result of changing the field, the field {@code distance} gets modified to a new value either.
     * @param newFrom the new value of the {@code from} field, represented by a {@link Location} object
     * @return the {@code Route} object with a new starting point
     * @throws ElementConstructionException if the new distance calculated from the new {@code from} field turned out
     * to be less than or equal to 1
     */
    public Route changeFrom(Location newFrom) throws ElementConstructionException {
        int newDistance = calculateDistance(newFrom.getX(), newFrom.getY(), newFrom.getZ(),
                to.getX(), to.getY(), to.getZ());
        return new Route(this.id, this.name, this.coordinates, this.creationDate, newFrom, this.to, newDistance);
    }

    /**
     * Returns a modified copy of this {@code Route} object with a changed destination point.
     * <b>Warning</b>: As a result of changing the field, the field {@code distance} gets modified to a new value either.
     * @param newTo the new value of the {@code from} field, represented by a {@link Location} object
     * @return the {@code Route} object with a new destination point
     * @throws ElementConstructionException if the new distance calculated from the new {@code to} field turned out
     * to be less than or equal to 1
     */
    public Route changeTo(Location newTo) throws ElementConstructionException {
        int newDistance = calculateDistance(from.getX(), from.getY(), from.getZ(),
                newTo.getX(), newTo.getY(), newTo.getZ());
        return new Route(this.id, this.name, this.coordinates, this.creationDate, this.from, newTo, newDistance);
    }

    public Route changeID(int id) throws ElementConstructionException {
        return new Route(id, this.name, this.coordinates, this.creationDate, this.from, this.to, this.distance);
    }

    /**
     * Returns the unique ID of the object.
     * @return the integer value of the {@code id} field contained in a wrapper
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the name of the object.
     * @return the string value of the {@code name} field
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the coordinates of the object.
     * @return the {@link Coordinates} object representing the current coordinates
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Returns the {@link LocalDateTime} object of this {@code Route} instance
     *
     * @return the unique {@link LocalDateTime} object captured at the object's creation moment
     */
    public String getFormattedDate() {
        return creationDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the {@link Location} object representing the departure point.
     * @return the {@link Location} object representing the departure point, i.e. the {@code from} field
     */
    public Location getFrom() {
        return from;
    }

    /**
     * Returns the {@link Location} object representing the destination point.
     * @return the {@link Location} object representing the destination point, i.e. the {@code to} field
     */
    public Location getTo() {
        return to;
    }

    /**
     * Returns the {@link Integer} value of the distance between the start and the end of the {@code Route}.
     * @return the integer value of the route distance calculated from the {@code from} and {@code to} fields
     * contained in a wrapper
     */
    public Integer getDistance() {
        return distance;
    }

    /**
     * Returns an inline representation of a {@code Route} object.
     * @return A single-line {@code String} representation of a {@code Route} object with values of single fields
     * separated by semicolons and inside values of them separated by commas
     */
    public String format() {
        return String.format("ID: %d; name: %s; creationDate=%s; coordinates: x=%d, y=%d; from: x=%d, y=%d, z=%f, " +
                        "name=%s; " + "to: x=%d, y=%d, z=%f, name=%s; distance=%d;", id, name,
                getFormattedDate(), coordinates.getX(), coordinates.getY(),
                from.getX(), from.getY(), from.getZ(), from.getName(),
                to.getX(), to.getY(), to.getZ(), to.getName(), distance);
//        return toString();
    }

    /**
     * Compares two {@code Route} objects numerically by their distance fields.
     * @param o the object to be compared.
     * @return the value 0 if this {@code Route}'s distance is equal to the argument's distance;
     * a value less than 0 if this {@code Route}'s distance is numerically less than the argument's distance;
     * and a value greater than 0 if this {@code Route}'s distance is numerically greater than
     * the argument's distance (signed comparison).
     */
    @Override
    public int compareTo(Route o) {
        return this.distance - o.distance;
    }

    // rewrite perhaps because objects can have the same id's apparently? -- not anymore haha
    /**
     * Compares this object to the specified object.
     * @param otherObject the object to be compared with.
     * @return true if the objects are the same; false otherwise.
     */
    @Override
    public boolean equals(Object otherObject) {
        // check if the two objects are identical
        if (this == otherObject) return true;
        // check that the other is not null
        if (otherObject == null) return false;
        // check that the other objects is Route
        if (this.getClass() != otherObject.getClass()) return false;
        return Objects.equals(this.id, ((Route) otherObject).getId());
    }

    /**
     * Returns a hash code for this {@code Route} object.
     * @return a hash code value for this object,
     * equal to the primitive int value represented by this {@code Route} object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, creationDate, coordinates, from, to);
    }

    /**
     * Returns a String object representing this {@code Route}'s value.
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Route[id=" + id + ",name=" + name + ",coordinates=" + coordinates.toString()
                + ",creationDate=" + creationDate.toString()
                + ",from=" + from.toString() + ",to=" + to.toString() + ",distance=" + distance + "]";
    }

    // calculates the distance between two points in a 3-d euclidean space
    private static int calculateDistance(long x1, long y1, float z1, long x2, long y2, float z2) {
        return (int) sqrt(pow((x2 - x1), 2) + pow((y2 - y1), 2) + pow((z2 - z1), 2));
    }

    public static class RouteBuilder implements Serializable {
        private String name;
        private Coordinates coordinates;
        private Location from;
        private Location to;
//        private int id;

        public void addName(String n) {
            name = n;
        }

        public void addCoordinates(Coordinates c) {
            coordinates = c;
        }

        public void addFrom(Location f) {
            from = f;
        }

        public void addTo(Location t) {
            to = t;
        }

        public void verifyDistance() throws ElementConstructionException {
            if (Route.calculateDistance(to.getX(), to.getY(), to.getZ(), from.getX(), from.getY(), from.getZ()) <= 1) {
                throw new ElementConstructionException("the calculated distance turned out to be less than or equal to 1");
            }
        }

//        public void setID(int i) {
//            id = i;
//        }


        public Route build() throws ElementConstructionException {
            verifyDistance();
            return new Route(name, coordinates, from, to);
        }
    }

    public void setCreator(String user) {
        userCreated = user;
    }

    public String getCreator() {
        return userCreated;
    }
}
