package sd2223.trab1.server.resources;

import sd2223.trab1.api.Feed;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.rest.FeedsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FeedResource implements FeedsService {
    private final Map<String, Feed> feeds = new HashMap<>();

    private static Logger Log = Logger.getLogger(UsersResource.class.getName());

    public FeedResource() {}


    @Override
    public long postMessage(String user, String pwd, Message msg) {

        // Check if the user exists and the pwd is correct



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
}
