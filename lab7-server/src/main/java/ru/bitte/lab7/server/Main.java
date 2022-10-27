package ru.bitte.lab7.server;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String usage = "Usage: server.jar -f [database properties] -p [port]";
        String fileName = null;
        int port = 0;
//        int fIndex = System.getenv().get("PROPERTIES");
//        int pIndex = System.getenv().get("port");
        try {
            fileName = (String) System.getenv().get("PROPERTIES");
            port = Integer.parseInt(System.getenv().get("PORT"));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println(usage);
            System.exit(0);
        }
        try {
            Server server = new Server(fileName, port);
            server.start();
        } catch (IOException e) {
            System.out.println("Error reading from a file: " + e.getMessage());
        }
    }
}
