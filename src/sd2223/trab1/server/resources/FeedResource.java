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
import java.time.LocalTime;
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

        String[] tokens = user.split("@");

        // Check if the domain in the message is the server domain
        if (!msg.getDomain().equals(domain)) {
            Log.info("Incorrect Message domain");
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        String serviceName = domain + ":users";
        // Check if the user exists and the pwd is correct
        var currentUser = getUser(tokens[0], pwd, serviceName);

        if( currentUser == null) {
            Log.info("Publisher does not exist in current domain.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        if( msg.getId() == -1) {
            // Generate mid
            msg.setId(3);
            // Propagate msg
        }
        msg.setCreationTime(System.currentTimeMillis());

        Feed userFeed = feeds.get(currentUser.getName());
        if(userFeed == null) {
            userFeed = new Feed(user,domain);
            feeds.put(currentUser.getName(),userFeed);
        }
        userFeed.postMessage(msg);

        return 3;
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {

    }

    @Override
    public Message getMessage(String user, long mid) {

        String[] tokens = user.split("@");

        Feed feed = feeds.get(tokens[0]);
        if (feed == null) {
            Log.info("User or Message does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        Message msg = feed.getMessage(mid);
        if(msg == null) {
            Log.info("Message does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        return msg;
    }

    @Override
    public List<Message> getMessages(String user, long time) {

        String[] tokens = user.split("@");

        Feed feed = feeds.get(tokens[0]);
        if (feed == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        List<Message> messageList = feed.getMessages(time);

        return messageList;
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
