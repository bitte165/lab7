package ru.bitte.lab7.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.PropertyConfigurator;
import ru.bitte.lab7.exceptions.UserAuthorizationException;
import ru.bitte.lab7.requests.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

@Log4j
public class Server {
    private final HikariDataSource dataSource;
    private final CollectionKeeper collectionKeeper;
    private final ExecutorService clientPool;
    private final ExecutorService commandPool;
    private final ForkJoinPool responsePool;
    private final ServerSocket server;
    private Socket newClient = null;
    private volatile boolean working = false;
    private final Set<String> activeUsers = Collections.synchronizedSet(new HashSet<>());

    public Server(String dbConfig, int port) throws IOException {
        // set up db
        HikariConfig config = new HikariConfig(dbConfig);
        dataSource = new HikariDataSource(config);
        dataSource.setConnectionTimeout(100000);
        // set up the db
        try (Connection conn = dataSource.getConnection()) {
            String createUsers = """
                    CREATE TABLE IF NOT EXISTS Users (
                        username varchar(255) NOT NULL PRIMARY KEY,
                        password varchar(65535) NOT NULL,
                        salt varchar(16) NOT NULL
                        );""";
            String createLocations = """
                    CREATE TABLE IF NOT EXISTS Locations (
                        location_id int NOT NULL PRIMARY KEY,
                        x int NOT NULL,
                        y int NOT NULL,
                        z real NOT NULL,
                        location_name varchar(255)
                        );""";
            String createRoutes = """
                    CREATE TABLE IF NOT EXISTS Routes (
                        route_id int NOT NULL PRIMARY KEY,
                        route_name varchar(255) NOT NULL,
                        creationDate timestamp NOT NULL,
                        coord_x int NOT NULL,
                        coord_y int NOT NULL,
                        from_id int NOT NULL,
                        to_id int NOT NULL,
                        distance int NOT NULL,
                        username varchar(255) NOT NULL
                    );""";
            Statement stat = conn.createStatement();
            stat.executeUpdate(createUsers);
            stat.executeUpdate(createLocations);
            stat.executeUpdate(createRoutes);
//            String createSequence = """
//                    CREATE SEQUENCE IF NOT EXISTS ?
//                    AS INT
//                    START WITH 1
//                    INCREMENT BY 1;""";
            String createSequence = """
                    CREATE SEQUENCE IF NOT EXISTS %s
                    AS INT
                    START WITH 1
                    INCREMENT BY 1;""";
//            PreparedStatement prepStat = conn.prepareStatement(createSequence);

            Set<String> ids = Set.of("user", "location", "route");
            for (String option : ids) {
//                prepStat.setString(1, String.format("%s_ids", option));
//                prepStat.executeUpdate();
                stat.executeUpdate(String.format(createSequence, option + "_ids"));
            }
            // fill up the collection and other
            collectionKeeper = new CollectionKeeper(dataSource);
            clientPool = Executors.newFixedThreadPool(16);
            commandPool = Executors.newFixedThreadPool(16);
//            responsePool = new ForkJoinPool();
            responsePool = new ForkJoinPool();
            server = new ServerSocket(port);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Runnable shutdown = () -> {
            log.info("Shutting down.");
            dataSource.close();
            clientPool.shutdown();
            commandPool.shutdown();
            responsePool.shutdown();
            try {
                server.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        // initialize logging
        String log4jConfigFile = System.getProperty("user.dir")
                + File.separator + "log4j.properties";
        PropertyConfigurator.configure(log4jConfigFile);
        // add shutting down
        Runtime.getRuntime().addShutdownHook(new Thread(shutdown));
    }

    public void start() throws IOException {
        log.info("Starting the server...");
        working = true;
        while (working) {
            newClient = server.accept();
            log.info("Accepted a new client");
            new Thread(new Runnable() {
                private final Socket client = newClient;
                public void run() {
                    // authorize the client
                    try {
                        var in = client.getInputStream();
//                        var out = client.getOutputStream();
                        ServerResponse response = null;
                        int header = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                        byte[] body = in.readNBytes(header);
                        Request deserObj = (Request) bytesToObject(body);
                        AuthorizeRequest request = null;
                        if (deserObj instanceof AuthorizeRequest) {
                            request = (AuthorizeRequest) deserObj;
                            log.info("Accepted a signing-up request");
                        } else if (deserObj instanceof SaltRequest) {
                            SaltRequest saltRequest = (SaltRequest) deserObj;
                            log.info("Accepted a login request");
                            ServerResponse saltResponse = null;
                            try (Connection conn = dataSource.getConnection()) {
                                PreparedStatement check = conn.prepareStatement("SELECT salt from Users where username=?");
                                check.setString(1, saltRequest.getUsername());
                                try (ResultSet checkRS = check.executeQuery()) {
                                    if (checkRS.isBeforeFirst()) {
                                        checkRS.next();
                                        saltResponse = new ServerResponse(checkRS.getString("salt"), false);
                                        assert saltResponse.getResponse().length() == 16;
                                    } else {
                                        saltResponse = new ServerResponse("No user with such username is signed up", true);
                                        log.info("User tried to log in with a non-existing username, was refused");
                                    }
                                }
                            } catch (SQLException e) {
                                log.error("Unknown database error occurred when authorizing the user");
                                log.debug(e.getMessage());
//                                throw new RuntimeException(e);
                            }
                            assert saltResponse != null;
                            if (saltResponse.getResponse().length() == 16 && activeUsers.contains(saltRequest.getUsername())) {
                                saltResponse = new ServerResponse("Somebody with this username is already logged in", true);
                                log.info("User is already logged, a new login attempt was refused");
                            }
//                            byte[] saltBytes = objectToBytes(saltResponse);
//                            byte[] saltHeader = ByteBuffer.allocate(4).putInt(saltBytes.length).array();
//                            out.write(saltHeader);
//                            out.write(saltBytes);
                            Future<Boolean> loginResult = responsePool.submit(new ResponseTask(saltResponse, client));
                            if (!saltResponse.isTerminating()) {
                                int requestHeader = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                                byte[] requestBody = in.readNBytes(requestHeader);
                                request = (AuthorizeRequest) bytesToObject(requestBody);
                            } else {
                                if (!loginResult.get()) {
                                    client.close();
                                }
                            }
                        }
                        if (request != null) {
                            User user = null;
                            try (DBManager newClientManager = new DBManager(dataSource.getConnection())) {
                                user = newClientManager.authorizeUser(request);
                                response = new ServerResponse("ok", false);
                                activeUsers.add(user.getUsername());
                                log.info(String.format("Client \"%s\" has authorized", user.getUsername()));
                            } catch (UserAuthorizationException e) {
                                response = new ServerResponse(e.getMessage(), true);
                                log.info("Failed to authorize the request");
                                log.info("Reason: " + e.getMessage());
                            } catch (SQLException e) {
                                log.error("Unknown database error occurred when authorizing the user");
                                log.debug(e.getMessage());
//                                throw new RuntimeException(e);
                            }
                            responsePool.submit(new ResponseTask(response, client));
                            assert response != null;
                            if (!response.isTerminating()) {
                                log.info("Starting a client handling task");
                                clientPool.submit(new ClientTask(new CommandHandler(collectionKeeper), client, user, commandPool, responsePool, activeUsers));
                            }
                        }
                    } catch (IOException e) {
                        log.error("Unknown IO exception occurred while processing a new client:");
                        log.error(e.getMessage());
//                        throw new RuntimeException(e);
                        //               System.out.println("Couldn't get a user's input stream, abandoning them!");
                    } catch (BufferUnderflowException e) {
                        log.error("Client disconnected abruptly");
                        try {
                            client.close();
                        } catch (IOException a) {
                            throw new RuntimeException(a);
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    public static Object bytesToObject(byte[] bytes) throws IOException {
        ObjectInputStream out;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)){
            out = new ObjectInputStream(bis);
            return out.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] objectToBytes(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(o);
        }
        return bos.toByteArray();
    }
}
