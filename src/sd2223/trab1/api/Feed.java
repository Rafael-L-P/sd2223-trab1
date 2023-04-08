package sd2223.trab1.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an user feed
 */
public class Feed {
    private String user;
    private String domain;
    private Map<Long,Message> messageList;
    private List<String> userSubs;

    public Feed(String user,String domain) {
        this.user = user;
        this.domain = domain;
        messageList = new HashMap<Long,Message>();
        userSubs = new ArrayList<String>();
    }

    public void postMessage(Message msg) {
        messageList.put(msg.getId(),msg);
    }

    public void removeMessage(long mid) {
        messageList.remove(mid);
    }

    public void subUser(String user) {
        userSubs.add(user);
    }

    public void unsubUser(String user) {
        userSubs.remove(user);
    }

    public Message getMessage(long mid) {
        return messageList.get(mid);
    }

    public List<Message> getMessages(long time) {
        //Todo
        return null;
    }

    public  List<String> getUserSubs() {
        return userSubs;
    }
}
