package ru.bitte.lab7.server;

import java.io.IOException;
import java.net.SocketException;

public class Main {
    public static void main(String[] args) {
        String usage = "Usage: server.jar -f [database properties] -p [port]";
        String fileName = null;
        int port = 0;
//        int fIndex = System.getenv().get("PROPERTIES");
//        int pIndex = System.getenv().get("port");
        try {
//            fileName = (String) System.getenv().get("PROPERTIES");
//            port = Integer.parseInt(System.getenv().get("PORT"));
            fileName = args[getIndexOf(args, "-f") + 1];
            port = Integer.parseInt(args[getIndexOf(args, "-p") + 1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println(usage);
            System.exit(0);
        }
        try {
            Server server = new Server(fileName, port);
            server.start();
        } catch (SocketException e) {
            if (e.getMessage().equals("Socket closed")) {
                System.out.println("Couldn't accept a new client because the server was closed");
            } else {
                System.out.println("SocketException: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("IO exception: " + e.getMessage());
        }
    }

    private static int getIndexOf(String[] strings, String item) {
        for (int i = 0; i < strings.length; i++) {
            if (item.equals(strings[i])) return i;
        }
        throw new ArrayIndexOutOfBoundsException();
    }
}
