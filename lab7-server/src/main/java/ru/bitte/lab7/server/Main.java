package ru.bitte.lab7.server;

import org.postgresql.util.PSQLException;

import java.io.IOException;

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
        } catch (IOException e) {
            System.out.println("Error reading from a file: " + e.getMessage());
        }
    }

    private static int getIndexOf(String[] strings, String item) {
        for (int i = 0; i < strings.length; i++) {
            if (item.equals(strings[i])) return i;
        }
        throw new ArrayIndexOutOfBoundsException();
    }
}
