package ru.bitte.lab7.route;

import ru.bitte.lab7.exceptions.ElementConstructionException;

import java.io.Serializable;
import java.util.Objects;

/**
 * A {@code Coordinates} object represents a particular two-dimensional point in space. The two coordinates
 * are represented by {@code long} fields with the {@code X} field being less than -974 and the {@code Y} field being
 * greater than 926. Otherwise, a {@link ElementConstructionException} is thrown
 */
public final class Coordinates implements Serializable {
    private final long x; // the max value should be 926
    private final Long y; // the min value should be -974, can't be null

    /**
     * Returns a {@code Coordinates} object with the passed parameters.
     * @param x the value of the <i>X</i> coordinate
     * @param y the value of the <i>Y</i> coordinate
     * @throws ElementConstructionException if either {@code X} is less than -974 or {@code Y} is greater than 926.
     */
    public Coordinates(long x, long y) throws ElementConstructionException {
        if (x < 926 && y > -974) {
            this.x = x;
            this.y = y;
        } else {
            throw new ElementConstructionException("Coordinates out of bounds -- [-974, 926]");
        }
    }

    /**
     * Returns a {@code long} value representing the location's <i>X</i> coordinate
     * @return the {@code long} value of the {@code X} field
     */
    public long getX() {
        return x;
    }

    /**
     * Returns a {@link Long} object representing a {@code long} value of the location's <i>Y</i> coordinate
     * @return the {@code Long} value of the {@code Y} field
     */
    public long getY() {
        return y;
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
        Coordinates other = (Coordinates) otherObject;
        return this.x == other.x && this.y.equals(other.y);
    }

    /**
     * Returns a hash code for this {@code Location} object.
     * @return a hash code value for this object,
     * equal to the primitive int value represented by this {@code Location} object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x,y);
    }

    /**
     * Returns a String object representing this {@code Coordinates}'s value.
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Coordinates[x=" + x + ",y=" + y + "]";
    }
}
