package ru.bitte.lab7.client;

import ru.bitte.lab7.requests.*;
import ru.bitte.lab7.exceptions.CommandParsingException;
import ru.bitte.lab7.exceptions.ElementConstructionException;
import ru.bitte.lab7.exceptions.UserAuthorizationException;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    private final Scanner inS = new Scanner(System.in);
//    private final ClientConnector connector = new ClientConnector();
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public void start(String host, int port) throws UserAuthorizationException {
        // starting connection
        try {
            socket = new Socket(host, port);
            out = socket.getOutputStream();
            in = socket.getInputStream();
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Unknown IO exception while starting server connection");
            throw new RuntimeException(e);
        }
        boolean active = true;
        try {
            authorize();
        } catch (IOException e) {
            System.out.println("Unknown IO exception while authorizing");
            throw new RuntimeException(e);
        }
        System.out.println("Welcome to lab6! See \"help\" for the list of commands.");
        while (active) {
            System.out.print("> ");
            String input = inS.nextLine();
            AbstractCommandRequest commandRequest;
            try {
                commandRequest = parseCommand(input);
            } catch (CommandParsingException | ElementConstructionException e) {
                System.out.println(e.getMessage());
                continue;
            }
//            if (commandRequest.getCommandName().equals("exit")) {
//                active = false;
//                System.out.println("Exiting...");
//
//            } else
            if (commandRequest.getCommandName().equals("execute_script")) {
                try {
                    assert commandRequest instanceof ArgumentCommandRequest;
                    String argument = ((ArgumentCommandRequest) commandRequest).getArgument();
                    List<String> commands = Files.readAllLines(Paths.get(argument));
                    for (String command : commands) {
                        try {
                            AbstractCommandRequest cr = parseCommand(command);
//                            if (cr.getCommandName().equals("exit")) {
//                                System.out.println("Can't exit while in a script!");
//                                continue;
//                            } else
                            if (cr.getCommandName().equals("execute_script")) {
                                System.out.println("Executing scripts inside of another one is prohibited");
                                continue;
                            }
                            ServerResponse sr = sendCommand(cr);
                            System.out.println(sr.getResponse());
                            if (sr.isTerminating()) {
                                active = false;
                            }

                        } catch (CommandParsingException | ElementConstructionException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("No script with such name found. Please provide a valid file name.");
                } catch (IOException e) {
                    System.out.println("Unknown IO exception while reading file:");
                    System.out.println(e.getMessage());
                }
            } else {
                ServerResponse serverResponse = sendCommand(commandRequest);
                System.out.println(serverResponse.getResponse());
                if (serverResponse.isTerminating()) {
                    active = false;
                }
            }
        }
        // closing the connection after exiting
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Unknown IO exception while closing the connection");
            throw new RuntimeException(e.getMessage());
        }
    }

    public void authorize() throws IOException, UserAuthorizationException {
        System.out.print("Do you want to login (L) or sign up (S)?: ");
        String choice = inS.nextLine().strip();
        if (choice.equalsIgnoreCase("L")) {
            System.out.print("Enter username: ");
            String username = inS.nextLine().strip();
            byte[] saltBody = objectToBytes(new SaltRequest(username));
            byte[] saltHead = ByteBuffer.allocate(4).putInt(saltBody.length).flip().array();
            out.write(saltHead);
            out.write(saltBody);
            int responseHead = ByteBuffer.wrap(in.readNBytes(4)).flip().getInt();
            byte[] responsebody = in.readNBytes(responseHead);
            String response = new String(responsebody, StandardCharsets.UTF_8);
            if (response.equals("dummy")) {
                throw new UserAuthorizationException("No user with such username in the database");
            } else if (response.length() == 16) {
                System.out.print("Enter password: ");
                String password = String.valueOf(inS.nextLine());
                AuthorizeRequest ar = new AuthorizeRequest(username, password, response);
                byte[] arBody = objectToBytes(ar);
                byte[] arHead = ByteBuffer.allocate(4).putInt(arBody.length).flip().array();
                out.write(arHead);
                out.write(arBody);
                int srHead = ByteBuffer.wrap(in.readNBytes(4)).flip().getInt();
                byte[] srBody = in.readNBytes(srHead);
                ServerResponse sr = (ServerResponse) bytesToObject(srBody);
                if (sr.isTerminating()) {
                    throw new UserAuthorizationException(sr.getResponse());
                } else {
                    return;
                }
            } else {
                assert false;
            }

        } else if (choice.equalsIgnoreCase("S")) {
            System.out.print("Enter username: ");
            String username = inS.nextLine().strip();
            System.out.print("Enter password: ");
            String password = String.valueOf(inS.nextLine());
            AuthorizeRequest ar = new AuthorizeRequest(username, password, true);
            byte[] arBody = objectToBytes(ar);
            byte[] arHead = ByteBuffer.allocate(4).putInt(arBody.length).flip().array();
            out.write(arHead);
            out.write(arBody);
            int srHead = ByteBuffer.wrap(in.readNBytes(4)).flip().getInt();
            byte[] srBody = in.readNBytes(srHead);
            ServerResponse sr = (ServerResponse) bytesToObject(srBody);
            if (sr.isTerminating()) {
                throw new UserAuthorizationException(sr.getResponse());
            } else {
                return;
            }
        } else {
            throw new UserAuthorizationException("Unknown option");
        }
    }

    // sending command to the server subroutine
    private ServerResponse sendCommand(AbstractCommandRequest commandRequest) {
        try {
//            connector.sendRequest(commandRequest);
            byte[] body = objectToBytes(commandRequest);
            byte[] header = ByteBuffer.allocate(4).putInt(body.length).flip().array();
            out.write(header);
            out.write(body);
        } catch (IOException e) {
            // todo
            System.out.println("Unknown IO exception while sending a command request");
            throw new RuntimeException(e);
        }
        try {
//            return connector.receiveResponse();
            byte[] header = in.readNBytes(4);
            int bodySize = ByteBuffer.wrap(header).getInt();
            byte[] body = in.readNBytes(bodySize);
            return (ServerResponse) bytesToObject(body);
        } catch (IOException e) {
            // todo
            System.out.println("Unknown IO exception while receiving command response");
            throw new RuntimeException(e);
        }
    }

       private AbstractCommandRequest parseCommand(String input) throws CommandParsingException, ElementConstructionException {
        Pattern nonArgCommand = Pattern.compile("^([a-zA-Z_]+)[^\\S\\r\\n]*$");
        Pattern argCommand = Pattern.compile("^([a-zA-Z_]+) ([\\w-.]+)[^\\S\\r\\n]*$");
        Matcher nonArgMatch = nonArgCommand.matcher(input);
        Matcher argMatch = argCommand.matcher(input);
        AbstractCommandRequest commandRequest;
        try {
            if (nonArgMatch.matches()) {
                commandRequest = new CommandRequest(nonArgMatch.group(1));
            } else if (argMatch.matches()) {
                commandRequest = new ArgumentCommandRequest(argMatch.group(1), argMatch.group(2));
                if (commandRequest.getCommandName().contains("id")) {
                    try {
                        Integer.parseInt(((ArgumentCommandRequest) commandRequest).getArgument());
                    } catch (NumberFormatException e) {
                        throw new ElementConstructionException("Error parsing the provided command ID");
                    }
                }
            } else {
                throw new ElementConstructionException("Incorrect command format. Please see \"help\" on command usage.");

            }
        } catch (ElementConstructionException e) {
            throw new ElementConstructionException(e.getMessage() + ". Please try again.");
        }
        return commandRequest;
    }

    public static Object bytesToObject(byte[] bytes) {
        ObjectInputStream out;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)){
            out = new ObjectInputStream(bis);
            return out.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] objectToBytes(Object o) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
                out.writeObject(o);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
