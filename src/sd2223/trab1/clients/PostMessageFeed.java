package sd2223.trab1.clients;

import java.io.IOException;
import java.util.logging.Logger;

public class PostMessageFeed {
    private static Logger Log = Logger.getLogger(CreateUserClient.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.err.println("Use: java sd2223.trab1.clients.PostMessageFeed user msg pwd");
            return;
        }

        String user = args[0];
        String msg = args[1];
        String pwd = args[2];


        Log.info("Sending request to server.");

        //  Todo
        //var result = new ();
        //System.out.println("Result: " + result);
    }


}
