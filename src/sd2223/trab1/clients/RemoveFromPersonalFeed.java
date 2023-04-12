package sd2223.trab1.clients;

import sd2223.trab1.api.Message;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class RemoveFromPersonalFeed {
    private static Logger Log = Logger.getLogger(CreateUserClient.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 4) {
            System.err.println("Use: java sd2223.trab1.clients.PostMessageFeed url user mid pwd");
            return;
        }

        String serverUrl = args[0];
        String user = args[1];
        Long mid = Long.parseLong(args[2]);
        String pwd = args[3];

        Log.info("Sending request to server.");

        new RestMessageClient(URI.create(serverUrl)).removeFromPersonalFeed(user,mid,pwd);

    }


}
