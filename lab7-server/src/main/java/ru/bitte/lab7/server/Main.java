package ru.bitte.lab7.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String usage = "Usage: server.jar -f [database properties] -p [port]";
        String fileName = null;
        int port = 0;
        try {
            if (args[0].equals("-f") && args[2].equals("-p") && args.length == 4) {
                fileName = args[1];
                port = Integer.parseInt(args[3]);
            } else {
                System.out.println(usage);
                System.exit(0);
            }
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
