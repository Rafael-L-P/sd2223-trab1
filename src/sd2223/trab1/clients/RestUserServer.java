package sd2223.trab1.clients;

import java.net.URI;

public class RestUserServer {

    private RestUsersClient userClient;

    private RestMessageClient messageClient;
    public RestUserServer( URI serverURI ) {
        userClient = new RestUsersClient(serverURI);
        messageClient = new RestMessageClient(serverURI);
    }

    public void deleteUser(String user, String secret) { messageClient.deleteUser(user,secret);}
}
