package sd2223.trab1.clients;

import sd2223.trab1.api.Message;

import java.io.IOException;
import java.net.URI;
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

        String serverUrl = args[0];
        String user = args[1];
        String pwd = args[2];
        String domain = args[3];
        String text = args[4];


        Message msg = new Message(-1,user,domain,text);

        Log.info("Sending request to server.");

        var result = new RestMessageClient(URI.create(serverUrl)).postMessage(user,pwd,msg);
        System.out.println("Result: " + result);
    }


}
