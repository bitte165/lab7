package ru.bitte.lab7.route;

import java.io.Serializable;
import java.util.Objects;

/**
 * A {@code Location} object represents a particular location, namely its coordinates and name. The {@code X} and
 * {@code Y} coordinates are represented by {@link Long} objects, and the {@code Z} coordinate is a {@code float} field.
 * The {@code name} is either the one provided or "Unnamed object" if it is empty.
 */
public final class Location implements Serializable {
    private final Long x;
    private final Long y; // can't be null
    private final float z; // can't be null
    private final String name; // can't be empty nor null

    /**
     * Constructs a {@code Location} object with the passed parameters. If the provided {@code name} string parameer is
     * empty, the name is automatically set to "Unnamed location".
     * @param x the value of the <i>X</i> coordinate
     * @param y the value of the <i>Y</i> coordinate
     * @param z the value of the <i>Z</i> coordinate
     * @param name the name of the location
     */
    public Location(Long x, Long y, float z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        if (name.strip().length() == 0) this.name = "Unnamed location";
        else this.name = Objects.requireNonNull(name, "null location name provided");
    }

    /**
     * Returns a {@link Long} object representing a {@code long} value of the location's <i>X</i> coordinate
     * @return the {@code Long} value of the {@code X} field
     */
    public Long getX() {
        return x;
    }

    /**
     * Returns a {@link Long} object representing a {@code long} value of the location's <i>Y</i> coordinate
     * @return the {@code Long} value of the {@code Y} field
     */
    public Long getY() {
        return y;
    }

    /**
     * Returns a {@code float} value representing the location's <i>Z</i> coordinate
     * @return the {@code float} value of the {@code Z} field
     */
    public float getZ() {
        return z;
    }

    /**
     * Returns a {@link String} object representing the location's name
     * @return the {@code String} value of the {@code name} field
     */
    public String getName() {
        return name;
    }

    /**
     * Compares this object to the specified object.
     * @param otherObject the object to be compared with.
     * @return true if the objects are the same; false otherwise.
     */
    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (otherObject == null) return false;
        if (this.getClass() != otherObject.getClass()) return false;
        Location other = (Location) otherObject;
        return this.x.equals(other.x) && this.y.equals(other.y)
                && this.z == other.z && this.name.equals(other.name);
    }

    /**
     * Returns a hash code for this {@code Location} object.
     * @return a hash code value for this object,
     * equal to the primitive int value represented by this {@code Location} object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x,y,z,name);
    }

    /**
     * Returns a String object representing this {@code Location}'s value.
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Coordinates[x=" + x + ",y=" + y + ",z=" + z + ",name=" + name + "]";
    }
}