package sd2223.trab1.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a user feed
 */
public class Feed {
    private String user;
    private String domain;
    private Map<Long,Message> messageList;
    private List<String> followers;
    private List<String> following;

    public Feed(String user,String domain) {
        this.user = user;
        this.domain = domain;
        messageList = new HashMap<Long,Message>();
        followers = new ArrayList<String>();
        following = new ArrayList<String>();
    }

    public void postMessage(Message msg) {
        messageList.put(msg.getId(),msg);
    }

    public void removeMessage(long mid) {
        messageList.remove(mid);
    }

    public void subUser(String user) {
        if (!following.contains(user))
            following.add(user);
    }

    public void unsubUser(String user) {
        following.remove(user);
    }

    public void addFollower(String user) {
        if (!followers.contains(user))
            followers.add(user);
    }

    public void removeFollower(String user) { followers.remove(user);}

    public Message getMessage(long mid) {
        return messageList.get(mid);
    }

    public List<Message> getMessages(long time) {
        List<Message> result = new ArrayList<Message>();
        messageList.values().stream().forEach(m -> {
            if (m.getCreationTime() > time) {
                Message msg = new Message(m.getId(),m.getUser(),m.getDomain(),m.getText());
                msg.setCreationTime(m.getCreationTime());
                result.add(msg);
            }
        });

        return result;
    }

    public  List<String> getUserSubs() {
        return following;
    }

    public List<String> getFollowers() {return followers;}

    public String getUser() {return user;}
}
