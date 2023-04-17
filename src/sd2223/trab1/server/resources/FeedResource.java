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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FeedResource implements FeedsService {
    private final static String USER_SERVICE = ":users";
    private final static String FEED_SERVICE = ":feeds";
    private final static String SECRET = "3Hc4q";

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
        this.count = serverID*10;
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
        if (!tokens[1].equals(domain)) {
            Log.info("Incorrect domain");
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        String serviceName = domain + USER_SERVICE;

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
        }
        msg.setCreationTime(System.currentTimeMillis());

        Feed userFeed = feeds.get(tokens[0]);
        if(userFeed == null) {
            userFeed = new Feed(user,domain);
            feeds.put(currentUser.getName(),userFeed);
        }

        userFeed.postMessage(msg);
        propagateMessage(userFeed.getFollowers(),user,msg);

        return count - 1;
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {
        Log.info("Remove from Feed : User: " + user + "; Message ID: " + mid + "; pwd: " + pwd);
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
        Log.info("GetMessage : User: " + user + "; Message id: " + mid);
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
        Log.info("GetMessages : User: " + user + "; Time: " + time);
        String[] tokens = user.split("@");

        Feed feed = feeds.get(tokens[0]);
        String serviceName = tokens[1] + USER_SERVICE;
        if (feed == null && hasUser(tokens[0],serviceName)) {;
            feed = new Feed(tokens[0],tokens[1]);
            feeds.put(tokens[0],feed);
        }

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

        String serviceName = tokens[1] + USER_SERVICE;

        if(!hasUser(tokens[0], serviceName)) {
            Log.info("User to be subscribed does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        var currentUser = getUser(tokens[0],pwd,serviceName);

        if(currentUser == null) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        String subServiceName = tokensSub[1] + USER_SERVICE;

        if(!hasUser(tokensSub[0], subServiceName)) {
            Log.info("User to be subscribed does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        Feed userFeed = feeds.get(tokens[0]);
        if(userFeed == null) {
            userFeed = new Feed(tokens[0],tokens[1]);
            feeds.put(tokens[0], userFeed);
        }

        if (tokensSub[1].equals(domain))
            addFollower(user,userSub,SECRET);
        else {
            subServiceName = tokensSub[1]+FEED_SERVICE;
            propagateSubUser(user,userSub,subServiceName);
        }

        userFeed.subUser(userSub);
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {
        Log.info("UnsubUser : User: " + user + "; Subscribed User: " + userSub + "; pwd: " + pwd);
        String[] tokens = user.split("@");
        String[] tokensSub = userSub.split("@");

        String serviceName = tokens[1] + USER_SERVICE;

        if(!hasUser(tokens[0], serviceName)) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        var currentUser = getUser(tokens[0],pwd,serviceName);

        if(currentUser == null) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        String subServiceName = tokensSub[1] + USER_SERVICE;
        if(!hasUser(tokensSub[0],subServiceName)) {
            Log.info("User to be subscribed does not exist.");
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        Feed userFeed = feeds.get(tokens[0]);

        if(userFeed != null) {
            userFeed.unsubUser(userSub);
            if (tokensSub[1].equals(domain))
                removeFollower(user, userSub, SECRET);
            else {
                serviceName = tokensSub[1] + FEED_SERVICE;
                propagateUnSubUser(user, userSub, serviceName);
            }
        } else {
            userFeed = new Feed(user,domain);
            feeds.put(currentUser.getName(),userFeed);
        }
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

    @Override
    public void updateFeeds(Message msg, String user,String secret) {
        Log.info("Update Feeds: User"+user+"; msg: "+msg.getText());
        if (!secret.equals(SECRET)) {
            Log.info("Request reserved to servers.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        List<Feed> feedList = feeds.values().stream().toList();
        for(Feed feed: feedList) {
            if(feed.getUserSubs().contains(user)) {
                feed.postMessage(msg);
            }
        }
    }

    @Override
    public void addFollower (String user, String userSub, String secret) {
        Log.info("Add Follower: User:"+user+"; userSub:"+ userSub);
        if (!secret.equals(SECRET)) {
            Log.info("Request reserved to servers.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        String[] tokens = userSub.split("@");

        Feed feed = feeds.get(tokens[0]);
        if (feed == null) {
            feed = new Feed(tokens[0],domain);
            feeds.put(tokens[0],feed);
        }
        feed.addFollower(user);
    }

    @Override
    public void removeFollower (String user, String subUser,String secret) {
        Log.info("Remove Follower: User:"+user+"; subUser:"+subUser);
        if (!secret.equals(SECRET)) {
            Log.info("Request reserved to servers.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        String[] tokens = subUser.split("@");

        Feed feed = feeds.get(tokens[0]);
        if (feed != null)
            feed.removeFollower(user);
        else {
            feed = new Feed(tokens[0],domain);
            feeds.put(tokens[0],feed);
        }
    }

    @Override
    public void deleteUser(String user, String secret) {
        Log.info("Delete user:"+user);
        if (!secret.equals(SECRET)) {
            Log.info("Request reserved to servers.");
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        feeds.remove(user);
    }

    private void propagateMessage(List<String> userSubs,String user,Message msg) {
        List<String> domains = new ArrayList<String>();
        for (String sub: userSubs) {
            String[] tokens = sub.split("@");
            if(!domains.contains(tokens[1]) && !domain.equals(tokens[1]))
                domains.add(tokens[1]);
        }


        for(String dom : domains) {
            URI[] uris = dis.knownUrisOf(dom+FEED_SERVICE, 1);
            new RestFeedServer(uris[0]).updateFeeds(msg,user,SECRET);
        }
        this.updateFeeds(msg,user,SECRET);
    }

    private void propagateUnSubUser(String user,String userSub,String serviceName) {
        URI[] uris = dis.knownUrisOf(serviceName, 1);

        new RestFeedServer(uris[0]).removeFollower(user,userSub,SECRET);
    }

    private void propagateSubUser(String user,String subUser,String serviceName) {
        URI[] uris = dis.knownUrisOf(serviceName, 1);

        new RestFeedServer(uris[0]).addFollower(user,subUser,SECRET);
    }
}
