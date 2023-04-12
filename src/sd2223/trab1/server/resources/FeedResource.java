package sd2223.trab1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.Discovery;
import sd2223.trab1.api.Feed;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.clients.RestFeedServer;

import java.net.URI;
import java.time.LocalTime;
import java.util.ArrayList;
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
            propagateMessage(msg, tokens[0]);
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

        String[] tokens = user.split("@");

        var currentUser = getUser(tokens[0],pwd,tokens[1]+":users");

        if(currentUser == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        if(!currentUser.getPwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        Feed userFeed = feeds.get(tokens[0]);

        if(userFeed.getMessage(mid) == null) {
            Log.info("Message does not exist in the server.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        userFeed.removeMessage(mid);
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

        String[] tokens = user.split("@");
        String[] tokensSub = userSub.split("@");

        var currentUser = getUser(tokens[0],pwd,tokens[1]+":users");

        if(currentUser == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        if(!currentUser.getPwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        Feed userFeed = feeds.get(tokensSub[0]);
        if(userFeed == null) {
            Log.info("User to be subscribed does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        userFeed.subUser(user);
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {

        String[] tokens = user.split("@");

        var currentUser = getUser(tokens[0],pwd,tokens[1]+":users");

        if(currentUser == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        if(!currentUser.getPwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        Feed userFeed = feeds.get(userSub);
        if(userFeed == null) {
            Log.info("User to be subscribed does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        userFeed.unsubUser(user);
    }

    @Override
    public List<String> listSubs(String user) {

        String[] tokens = user.split("@");

        Feed userFeed = feeds.get(tokens[0]);
        if(userFeed == null) {
            Log.info("User to be subscribed does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        return userFeed.getUserSubs();
    }

    @Override
    public void updateFeeds(Message msg, String user) {
        feeds.forEach((k,feed) -> {
            if(feed.getUserSubs().contains(user))
                feed.postMessage(msg);
        });
    }

    private User getUser(String user, String pwd,String serviceName) {
        // Use discovery to get the userservice uri
        URI[] uris = dis.knownUrisOf(serviceName,1);

        var result = new RestFeedServer(uris[0]).getUser(user,pwd);
        return result;
    }

    private void propagateMessage(Message msg, String userName) {
        List<String> userSubs = feeds.get(userName).getUserSubs();

        List<String> domains = new ArrayList<String>();

        userSubs.forEach( (sub) -> {
            String[] tokens = sub.split("@");
            if(!domains.contains(tokens[1]))
                domains.add(tokens[1]);
        });

        for(String domain : domains) {
            URI[] uris = dis.knownUrisOf(domain+":users", 1);

            new RestFeedServer(uris[0]).propagateMessage(msg,userName);
        }
    }
}
