package ru.bitte.lab7.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.bitte.lab7.exceptions.UserAuthorizationException;
import ru.bitte.lab7.requests.*;

import javax.sql.DataSource;
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


public class Server {
    private final HikariDataSource dataSource;
    private final CollectionKeeper collectionKeeper;
    private final ExecutorService clientPool;
    private final ExecutorService commandPool;
    private final ForkJoinPool responsePool;
    private final ServerSocket server;
    private Socket newClient = null;

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
            responsePool = new ForkJoinPool();
            server = new ServerSocket(port);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Runnable shutdown = () -> {
            System.out.println("Shutting down.");
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
        Runtime.getRuntime().addShutdownHook(new Thread(shutdown));
    }

    public void start() throws IOException {
        while (true) {
            newClient = server.accept();
            new Thread(new Runnable() {
                private final Socket client = newClient;
                public void run() {
                    // authorize the client
                    try {
                        var in = client.getInputStream();
                        var out = client.getOutputStream();
                        ServerResponse response;
                        int header = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                        byte[] body = in.readNBytes(header);
                        Request deserObj = (Request) bytesToObject(body);
                        AuthorizeRequest request = null;
                        if (deserObj instanceof AuthorizeRequest) {
                            request = (AuthorizeRequest) deserObj;
                        } else if (deserObj instanceof SaltRequest) {
                            SaltRequest saltRequest = (SaltRequest) deserObj;
                            String salt = null;
                            try (Connection conn = dataSource.getConnection()) {
                                PreparedStatement check = conn.prepareStatement("SELECT salt from Users where username=?");
                                check.setString(1, saltRequest.getUsername());
                                try (ResultSet checkRS = check.executeQuery()) {
                                    if (checkRS.isBeforeFirst()) {
                                        salt = checkRS.getString("salt");
                                    } else {
                                        salt = "dummy";
                                    }
                                }
                            }
                            assert salt != null;
                            byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
                            byte[] saltHeader = ByteBuffer.allocate(4).putInt(saltBytes.length).flip().array();
                            out.write(saltHeader);
                            out.write(saltBytes);
                            if (!salt.equals("dummy")) {
                                int requestHead = ByteBuffer.wrap(in.readNBytes(4)).getInt();
                                byte[] requestBody = in.readNBytes(requestHead);
                                request = (AuthorizeRequest) bytesToObject(requestBody);
                            }
                        }
                        if (request != null) {
                            User user = null;
                            try (DBManager newClientManager = new DBManager(dataSource.getConnection())) {
                                user = newClientManager.authorizeUser(request);
                                response = new ServerResponse("ok", false);
                            } catch (UserAuthorizationException e) {
                                response = new ServerResponse(e.getMessage(), true);
                            }
                            if (response.isTerminating()) {
                                responsePool.submit(new ResponseTask(response, client));
                            } else {
                                responsePool.submit(new ResponseTask(response, client));
                                clientPool.submit(new ClientTask(new CommandHandler(collectionKeeper, user), client, commandPool, responsePool));
                            }
                        }

                    } catch (IOException | SQLException e) {
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
