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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Log4j
public class Server {
    private final HikariDataSource dataSource;
    private final CollectionKeeper collectionKeeper;
    private final ExecutorService clientPool;
    private final ExecutorService commandPool;
    private final ForkJoinPool responsePool;
    private final ServerSocket server;
    private Socket newClient = null;
//    private static Logger logger = Logger.getLogger(this.getClass());

    public Server(String dbConfig, int port) throws IOException {
        // set up db
        HikariConfig config = new HikariConfig(dbConfig);
        dataSource = new HikariDataSource(config);
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
        while (true) {
            newClient = server.accept();
            log.info("Accepted a new client");
            new Thread(new Runnable() {
                private final Socket client = newClient;
                public void run() {
                    // authorize the client
                    try {
                        var in = client.getInputStream();
                        var out = client.getOutputStream();
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
                            String salt = null;
                            try (Connection conn = dataSource.getConnection()) {
                                PreparedStatement check = conn.prepareStatement("SELECT salt from Users where username=?");
                                check.setString(1, saltRequest.getUsername());
                                try (ResultSet checkRS = check.executeQuery()) {
                                    if (checkRS.isBeforeFirst()) {
                                        checkRS.next();
                                        salt = checkRS.getString("salt");
                                    } else {
                                        salt = "dummy";
                                    }
                                }
                            } catch (SQLException e) {
                                log.error("Unknown database error occurred when authorizing the user");
                                log.debug(e.getMessage());
//                                throw new RuntimeException(e);
                            }
                            assert salt != null;
                            byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
                            byte[] saltHeader = ByteBuffer.allocate(4).putInt(saltBytes.length).array();
                            out.write(saltHeader);
                            out.write(saltBytes);
                            if (!salt.equals("dummy")) {
                                int requestHead = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                                byte[] requestBody = in.readNBytes(requestHead);
                                request = (AuthorizeRequest) bytesToObject(requestBody);
                                log.info("Accepted a login request");
                            }
                        }
                        if (request != null) {
                            User user = null;
                            try (DBManager newClientManager = new DBManager(dataSource.getConnection())) {
                                user = newClientManager.authorizeUser(request);
                                response = new ServerResponse("ok", false);
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
                                clientPool.submit(new ClientTask(new CommandHandler(collectionKeeper), client, user, commandPool, responsePool));
                            }
                        }
                    } catch (IOException e) {
                        log.error("Unknown IO exception occurred while processing a new client");
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                        //               System.out.println("Couldn't get a user's input stream, abandoning them!");
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
