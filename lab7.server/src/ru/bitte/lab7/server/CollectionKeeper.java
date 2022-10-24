package ru.bitte.lab7.server;

import ru.bitte.lab7.exceptions.GetByIDException;
import ru.bitte.lab7.requests.User;
import ru.bitte.lab7.route.Route;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An object of this class holds a collection of elements and provides an interface for accessing and modifying them.
 * The collection is a {@link HashSet} containing objects of the {@link Route} class. An instance of this class can be
 * created with a collection of initial {@code Route} objects that get put into the collection first.
 */
public class CollectionKeeper {
    private final Set<Route> collection; // main collection
    private final LocalDateTime creationDate;
    private final ReentrantLock lock;
    private DataSource dataSource;

    /**
     * Returns an instance of the {@code CollectionKeeper} class.
     * @param collection the initial objects to be put in the collection
     */
    public CollectionKeeper(DataSource dataSource){
        this.creationDate = LocalDateTime.now();
        this.collection = Collections.synchronizedSet(new HashSet<>());
        this.lock = new ReentrantLock();
        // read objects

    }

    /**
     * Adds a new element to the collection.
     * @param element the {@code Route} to be put in the collection
     */
    public void addElement(Route element, User credentials) {
        lock.lock();
        try {
            collection.add(Objects.requireNonNull(element));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes an existing element from the collection.
     * @param element the element to be removed from the collection
     */
    public void removeElement(Route element, User credentials) {
        lock.lock();
        try {
            collection.remove(Objects.requireNonNull(element));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes all the elements in the collection.
     */
    public void clearCollection(User credentials) {
        lock.lock();
        try {
            collection.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a copy of the existing collection.
     * @implNote The returned copy is a shallow one, but that isn't an issue since {@code Route} objects are immutable
     * @return a copy of the collection
     */
    public HashSet<Route> copyCollection() {
        lock.lock();
        try {
            return new HashSet<>(collection);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the name of the class of objects in the collection ({@code Route}).
     * @return the string containing the collection objects class
     */
    public String getCollectionType() {
        return Route.class.toString();
    }

    /**
     * Returns the {@link LocalDateTime} object representing the collection creation date.
     * @return the {@link LocalDateTime} object representing the collection creation date
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the number of elements in the collection.
     * @return the integer number of elements in the collection
     */
    public int getCollectionSize() {
        lock.lock();
        try {
            return collection.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a {@link TreeSet} containing the elements from the collection.
     * @return a {@link TreeSet} containing the elements from the collection
     */
    public TreeSet<Route> copySorted() {
        lock.lock();
        try {
            return new TreeSet<>(collection);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an element from the collection with the given ID.
     * @param id the unique identification number of a wanted element in the collection
     * @return the element from the collection by the given ID
     * @throws GetByIDException if no element with such an ID was found in the collection
     */
    public Route getByID(int id) throws GetByIDException {
        lock.lock();
        try {
            Optional<Route> wantedElement = collection.stream().filter((Route r ) -> r.getId() == id).findAny();
            if (wantedElement.isEmpty()) {
                throw new GetByIDException("No Route object with such an ID in the collection");
            } else {
                return wantedElement.get();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes an element with the given ID from the collection.
     * @param id the unique identification number of a wanted element in the collection
     * @throws GetByIDException if no element with such an ID was found in the collection
     */
    public void removeByID(int id, User credentials) throws GetByIDException {
        lock.lock();
        try {
            removeElement(getByID(id), credentials);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Replaces an element of a specific ID by the provided element of the same ID. The specific ID is the one of the
     * element from the parameter.
     * @param element the element with a specific ID that replaces an element in the collection with that ID
     * @throws GetByIDException if no element with such an ID was found in the collection
     */
    public void replaceByID(Route element, User credentials) throws GetByIDException {
        lock.lock();
        try {
            int id = element.getId();
            Route oldElement = getByID(id);
            removeElement(oldElement, credentials);
            addElement(element, credentials);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a list of the elements from the collection that have a particular substring in the name.
     * @param filter the string that must be contained in the names of the returned objects
     * @return an {@link ArrayList} of the filtered elements
     */
    public List<Route> filterByString(String filter) {
        Stream<Route> elements = collection.stream();
        return elements.filter(route -> !(route.getName().contains(filter))).collect(Collectors.toList());
    }

    /**
     * Returns a list of the elements from the collection that have greater {@code distance} fields than that of the
     * provided element.
     * @param element the element the distance is compared to other elements
     * @return an {@link ArrayList} of the greater distance elements
     */
    public List<Route> getElementsGreaterThan(Route element) {
        Stream<Route> elements = collection.stream();
        return elements.filter(route -> route.getDistance() > element.getDistance()).collect(Collectors.toList());
    }
}
