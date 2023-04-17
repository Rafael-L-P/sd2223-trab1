package sd2223.trab1.clients;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;

import java.net.URI;
import java.util.List;

public class RestFeedServer {

    private RestUsersClient userClient;

    private RestMessageClient messageClient;

    public RestFeedServer( URI serverURI ) {
         userClient = new RestUsersClient(serverURI);
         messageClient = new RestMessageClient(serverURI);
    }

    public User getUser(String name, String pwd) { return userClient.getUser(name,pwd);}

    public boolean hasUser(String name) { return userClient.hasUser(name);}

    public void updateFeeds(Message msg, String user, String secret) { messageClient.updateFeeds(msg,user,secret);}

    public void addFollower(String user,String subUser,String secret) { messageClient.addFollower(user,subUser,secret);}

    public void removeFollower(String user,String userSub,String secret) { messageClient.removeFollower(user,userSub,secret);}

    public Message getRemoteMessage(String user,long mid) { return messageClient.getMessage(user,mid);}

    public List<Message> getRemoteMessages(String user,long time) { return messageClient.getMessages(user,time);}
}
