package sd2223.trab1.clients;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;

import java.net.URI;

public class RestFeedServer {

    private RestUsersClient userClient;

    private RestMessageClient messageClient;

    public RestFeedServer( URI serverURI ) {
         userClient = new RestUsersClient(serverURI);
         messageClient = new RestMessageClient(serverURI);
    }

    public User getUser(String name, String pwd) { return userClient.getUser(name,pwd);
    }

    public boolean hasUser(String name) { return userClient.hasUser(name);}

    //public void propagateMessage(Message msg, String user) { messageClient.propagateMessage(msg,user);}
}
