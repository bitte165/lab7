package ru.bitte.lab7.client;

import ru.bitte.lab7.exceptions.UserAuthorizationException;

public class Main {
    public static void main(String[] args) {
        String usage = "Usage: client.jar -h [host address] -p [port]";
        String hostName = null;
        int port = 0;
        try {
            if (args[0].equals("-h") && args[2].equals("-p") && args.length == 4) {
                hostName = args[1];
                port = Integer.parseInt(args[3]);
            } else {
                System.out.println(usage);
                System.exit(0);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println(usage);
            System.exit(0);
        }
        Client client = new Client();
        try {
            client.start(hostName, port);
        } catch (UserAuthorizationException e) {
            System.out.println("Couldn't authorize because:");
            System.out.println(e.getMessage());
        }
    }
}
