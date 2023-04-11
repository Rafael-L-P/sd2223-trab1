package sd2223.trab1.clients;

import sd2223.trab1.api.User;

import java.net.URI;

public class RestFeedServer {

    private RestUsersClient client;

    public RestFeedServer( URI serverURI ) {
         client = new RestUsersClient(serverURI);
    }

    public User getUser(String name, String pwd) {
        return client.getUser(name,pwd);
    }
}
