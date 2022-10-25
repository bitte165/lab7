package ru.bitte.lab7.server;

import ru.bitte.lab7.exceptions.ElementConstructionException;
import ru.bitte.lab7.exceptions.UserAuthorizationException;
import ru.bitte.lab7.exceptions.UserUnauthorizedException;
import ru.bitte.lab7.requests.AuthorizeRequest;
import ru.bitte.lab7.requests.User;
import ru.bitte.lab7.route.Coordinates;
import ru.bitte.lab7.route.Location;
import ru.bitte.lab7.route.Route;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DBManager implements AutoCloseable {
    private final Connection connection;

    public DBManager(Connection conn) {
        connection = conn;
    }

    public User authorizeUser(AuthorizeRequest ar) throws UserAuthorizationException {
        try {
            if (ar.isSigningUp()) {
                PreparedStatement check = connection.prepareStatement("SELECT username from Users where username=?");
                check.setString(1, ar.getUsername());
                try (ResultSet checkRS = check.executeQuery()) {
                    if (checkRS.next()) {
                        throw new UserAuthorizationException("User with such username is already registered");
                    }
                }
                PreparedStatement signUp = connection.prepareStatement("INSERT INTO Users VALUES (?, ?, ?);");
                signUp.setString(1, ar.getUsername());
                signUp.setString(2, ar.getPassword());
                signUp.setString(3, ar.getSalt());
                signUp.executeUpdate();
                return ar.toUser();
            } else {
                PreparedStatement readPasswd = connection.prepareStatement("SELECT password from Users where username=?");
                readPasswd.setString(1, ar.getUsername());
                String password = null;
                try (ResultSet passwdRS = readPasswd.executeQuery()) {
                    if (passwdRS.isBeforeFirst()) {
                        passwdRS.next();
                        password = passwdRS.getString("password");
                    } else {
                        assert false;
                    }
                }
                assert password != null;
                if (ar.getPassword().equals(password)) {
                    return ar.toUser();
                } else {
                    throw new UserAuthorizationException("Incorrect password");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Route> readElements() throws SQLException {
        Set<Route> result = new HashSet<>();
        Map<Integer, Location> locations = new TreeMap<>();
        PreparedStatement readLocations = connection.prepareStatement("SELECT * FROM Locations");
        try (ResultSet locationResult = readLocations.executeQuery()) {
            while (locationResult.next()) {
                int id = locationResult.getInt("location_id");
                int x = locationResult.getInt("x");
                int y = locationResult.getInt("y");
                double z = locationResult.getDouble("z");
                String name = locationResult.getString("location_name");
                locations.put(id, new Location((long) x, (long) y, (float) z, name));
            }
        }
        PreparedStatement readRoutes = connection.prepareStatement("SELECT * FROM Routes");
        try (ResultSet routeResult = readRoutes.executeQuery()) {
            while (routeResult.next()) {
                int id = routeResult.getInt("route_id");
                String name = routeResult.getString("route_name");
                LocalDateTime dateTime = routeResult.getTimestamp("creationdate").toLocalDateTime();
                Coordinates coords = null;
                try {
                    coords = new Coordinates(routeResult.getInt("coord_x"), routeResult.getInt("coord_y"));
                } catch (ElementConstructionException e) {}
                Location from = locations.get(routeResult.getInt("from_id"));
                Location to = locations.get(routeResult.getInt("to_id"));
                int distance = routeResult.getInt("distance");
                String creator = routeResult.getString("username");
                Route r = null;
                try {
                    r = new Route(id, name, coords, dateTime, from, to, distance);
                    r.setCreator(creator);
                } catch (ElementConstructionException e) {}
                result.add(r);
            }
        }
        return result;
    }

    public int addElement(Route element, String username) throws SQLException {
        PreparedStatement addLocation = connection.prepareStatement("INSERT INTO Locations VALUES (nextval('location_ids'), ?, ?, ?, ?);");
        addLocation.setInt(1, Math.toIntExact(element.getFrom().getX()));
        addLocation.setInt(2, Math.toIntExact(element.getFrom().getY()));
        addLocation.setDouble(3, element.getFrom().getZ());
        addLocation.setString(4, element.getFrom().getName());
        addLocation.executeUpdate();
        addLocation.setInt(1, Math.toIntExact(element.getTo().getX()));
        addLocation.setInt(2, Math.toIntExact(element.getTo().getY()));
        addLocation.setDouble(3, element.getTo().getZ());
        addLocation.setString(4, element.getTo().getName());
        addLocation.executeUpdate();
        PreparedStatement getIDs = connection.prepareStatement("SELECT location_id from Locations ORDER BY location_id DESC LIMIT 2");
        int from_id, to_id;
        try (ResultSet ids = getIDs.executeQuery()) {
            ids.next();
            to_id = ids.getInt("location_id");
            ids.next();
            from_id = ids.getInt("location_id");
        }
        PreparedStatement addRoute = connection.prepareStatement("INSERT INTO Routes VALUES (nextval('route_ids'), ?, ?, ?, ?, ?, ?, ?, ?);");
        addRoute.setString(1, element.getName());
        addRoute.setTimestamp(2, Timestamp.valueOf(element.getCreationDate()));
        addRoute.setInt(3, (int) element.getCoordinates().getX());
        addRoute.setInt(4, (int) element.getCoordinates().getY());
        addRoute.setInt(5, from_id);
        addRoute.setInt(6, to_id);
        addRoute.setInt(7, element.getDistance());
        addRoute.setString(8, username);
        addRoute.executeUpdate();
        Statement getID = connection.createStatement();
        try (ResultSet id = getID.executeQuery("SELECT route_id FROM Routes ORDER BY route_id DESC LIMIT 1")) {
            id.next();
            return id.getInt("route_id");
        }

    }

    public void removeElement(Route element, String user) throws UserUnauthorizedException, SQLException {
        PreparedStatement getCreator = connection.prepareStatement("SELECT username from Routes WHERE route_id=?");
        getCreator.setInt(1, element.getId());
        String creator = null;
        try (ResultSet creatorResult = getCreator.executeQuery()) {
            if (creatorResult.isBeforeFirst()) {
                creatorResult.next();
                creator = creatorResult.getString("username");
            } else {
                throw new RuntimeException("fuck");
            }
        }
        if (!user.equals(creator)) {
            throw new UserUnauthorizedException("Couldn't remove the object \"%s\" because there are no permissions to do that");
        } else {
            PreparedStatement removeElement = connection.prepareStatement("DELETE FROM Routes WHERE route_id=?");
            removeElement.setInt(1, element.getId());
            removeElement.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
