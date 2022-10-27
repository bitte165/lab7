package ru.bitte.lab7.client;

import ru.bitte.lab7.exceptions.UserAuthorizationException;

public class Main {
    public static void main(String[] args) {
        String usage = "Usage: client.jar -h [host address] -p [port]";
        String hostName = null;
        int port = 0;
        try {
            hostName = args[getIndexOf(args, "-h") + 1];
            port = Integer.parseInt(args[getIndexOf(args, "-p") + 1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println(usage);
            System.exit(0);
        }
        Client client = new Client(hostName, port);
        try {
            client.start();
        } catch (UserAuthorizationException e) {
            System.out.print("Couldn't authorize because: ");
            System.out.println(e.getMessage());
        }
    }

    private static int getIndexOf(String[] strings, String item) {
        for (int i = 0; i < strings.length; i++) {
            if (item.equals(strings[i])) return i;
        }
        throw new ArrayIndexOutOfBoundsException();
    }
}
