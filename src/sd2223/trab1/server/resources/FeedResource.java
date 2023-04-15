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
    private int count;

    public FeedResource(String domain, int serverID) {
        this.domain = domain;
        this.serverID = serverID;
        this.dis = Discovery.getInstance();
        this.count = 0;
    }


    @Override
    public long postMessage(String user, String pwd, Message msg) {

        Log.info("Post Message: user: " + user + " pwd: " + pwd + " msg: " + msg);

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

        if(!hasUser(tokens[0], serviceName)) {
            Log.info("Publisher does not exist in current domain.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        // Check if the user exists and the pwd is correct
        var currentUser = getUser(tokens[0], pwd, serviceName);

        if(currentUser == null) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        if( msg.getId() == -1) {
            // Generate mid
            msg.setId(count++);
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

        return count - 1;
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {

        String[] tokens = user.split("@");

        String serviceName = tokens[1] + ":users";
        var currentUser = getUser(tokens[0],pwd,serviceName);

        if(currentUser == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
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

        Log.info("SubUser : User: " + user + "; Subscribed User: " + userSub + "; pwd: " + pwd);

        String[] tokens = user.split("@");
        String[] tokensSub = userSub.split("@");

        String serviceName = tokens[1] + ":users";

        if(!hasUser(tokens[0], serviceName)) {
            Log.info("User to be subscribed does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        var currentUser = getUser(tokens[0],pwd,serviceName);

        if(currentUser == null) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        String subServiceName = tokensSub[1] + ":users";

        if(!hasUser(tokensSub[0], subServiceName)) {
            Log.info("User to be subscribed does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        Feed subFeed = feeds.get(tokensSub[0]);
        if(subFeed == null) {
            subFeed = new Feed(tokensSub[0],tokensSub[1]);
            feeds.put(tokensSub[0], subFeed);
        }

        Feed userFeed = feeds.get(tokens[0]);
        if(userFeed == null) {
            userFeed = new Feed(tokens[0],tokens[1]);
            feeds.put(tokens[0], userFeed);
        }

        userFeed.subUser(userSub);
        subFeed.addFollower(user);
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {

        String[] tokens = user.split("@");
        String[] tokensSub = userSub.split("@");

        String serviceName = tokens[1] + ":users";

        if(!hasUser(tokens[0], serviceName)) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        var currentUser = getUser(tokens[0],pwd,serviceName);

        if(currentUser == null) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        String subServiceName = tokensSub[1] + ":users";
        if(!hasUser(tokensSub[0],subServiceName)) {
            Log.info("User to be subscribed does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        Feed subFeed = feeds.get(tokensSub[0]);
        if(subFeed == null) {
            subFeed = new Feed(tokensSub[0],tokensSub[1]);
            feeds.put(tokensSub[0], subFeed);
        }

        Feed userFeed = feeds.get(tokens[0]);
        if(userFeed == null) {
            userFeed = new Feed(tokens[0],tokens[1]);
            feeds.put(tokens[0], userFeed);
        }

        userFeed.unsubUser(userSub);
        subFeed.removeFollower(user);
    }

    @Override
    public List<String> listSubs(String user) {

        String[] tokens = user.split("@");

        String serviceName = tokens[1] + ":users";

        if(!hasUser(tokens[0],serviceName)) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        Feed userFeed = feeds.get(tokens[0]);
        if(userFeed == null) {
            userFeed = new Feed(tokens[0],tokens[1]);
            feeds.put(tokens[0], userFeed);
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

    private boolean hasUser(String user, String serviceName) {
        URI[] uris = dis.knownUrisOf(serviceName, 1);

        var result = new RestFeedServer(uris[0]).hasUser(user);
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

        for(String dom : domains) {
            URI[] uris = dis.knownUrisOf(dom+":users", 1);

            new RestFeedServer(uris[0]).propagateMessage(msg,userName);
        }
    }
}
