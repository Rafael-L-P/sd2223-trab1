package sd2223.trab1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.Discovery;
import sd2223.trab1.api.Feed;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.clients.RestFeedServer;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FeedResource implements FeedsService {
    private final Map<String, Feed> feeds = new HashMap<>();

    private static Logger Log = Logger.getLogger(UsersResource.class.getName());

    private String domain;
    private int serverID;
    private Discovery dis;

    public FeedResource(String domain, int serverID) {
        this.domain = domain;
        this.serverID = serverID;
        this.dis = Discovery.getInstance();
    }


    @Override
    public long postMessage(String user, String pwd, Message msg) {

        if (user == null || pwd == null || msg == null) {
            Log.info("Name, Password or Message null.");
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        String serviceName = domain + ":users";
        // Check if the user exists and the pwd is correct
        var currentUser = getUser(user, pwd, serviceName);


        // Check if the domain in the message is the server domain
        if (!msg.getDomain().equals(domain)) {
            Log.info("Incorret Message domain");
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        if( msg.getId() == -1) {
            // Generate mid
            msg.setId(3);
            // Propagate msg
        }

        Feed userFeed = feeds.get(currentUser.getName());
        userFeed.postMessage(msg);

        return 0;
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {

    }

    @Override
    public Message getMessage(String user, long mid) {
        return null;
    }

    @Override
    public List<Message> getMessages(String user, long time) {
        return null;
    }

    @Override
    public void subUser(String user, String userSub, String pwd) {

    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {

    }

    @Override
    public List<String> listSubs(String user) {
        return null;
    }

    private User getUser(String user, String pwd,String serviceName) {
        // Use discovery to get the userservice uri
        URI[] uris = dis.knownUrisOf(serviceName,1);

        var result = new RestFeedServer(uris[0]).getUser(user,pwd);
        return result;
    }

}
