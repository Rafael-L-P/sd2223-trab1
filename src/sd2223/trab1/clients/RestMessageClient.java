package sd2223.trab1.clients;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;

import java.net.URI;
import java.util.List;

public class RestMessageClient extends RestClient implements FeedsService {

    final WebTarget target;

    RestMessageClient(URI serverURI) {
        super(serverURI);
        target = client.target(serverURI).path(FeedsService.PATH);
    }

    private long clt_postMessage(String user, String pwd, Message msg) {

        Response r = target.path(user)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(msg,MediaType.APPLICATION_JSON));

        if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() )
            return r.readEntity(Long.class);
        else
            System.out.println("Error, HTTP error status " + r.getStatus());

        return -1;
    }

    private void clt_subUser(String user, String userSub, String pwd) {

    }

    private void clt_removeFromPersonalFeed(String user, long mid, String pwd) {

        Response r = target.path(user + "/" + mid)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        if( r.getStatus() != Status.OK.getStatusCode() || !r.hasEntity())
            System.out.println("Error, HTTP error status " + r.getStatus());
    }

    private Message clt_getMessage(String user,long mid){
        Response r = target.path("/"+user+"/"+mid ).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() )
            return r.readEntity(Message.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }

    private List<Message> clt_getMessages(String user,long time) {
        Response r = target.path("/"+user )
                .queryParam(FeedsService.TIME, time).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if( r.getStatus() == Status.OK.getStatusCode() && r.hasEntity() )
            return r.readEntity(List.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }

    private int clt_updateFeeds(Message msg, String user, String secret) {
        Response r = target.path( "/update/feed/" + user)
                .queryParam(FeedsService.SECRET, secret).request()
                .post(Entity.entity(msg, MediaType.APPLICATION_JSON));

        if( r.getStatus() != Status.OK.getStatusCode())
            System.out.println("Error, HTTP error status " + r.getStatus());

        return 0;
    }

    private int clt_addFollower(String user,String userSub,String secret) {
        System.out.println("Client addFollower:"+user+"; usersub:"+userSub+"; secret:"+secret);
        Response r = target.path("/update/subs/" + user + "/" + userSub)
                .queryParam(FeedsService.SECRET,secret).request()
                .post(Entity.json(null));

        if( r.getStatus() != Status.OK.getStatusCode())
            System.out.println("Error, HTTP error status in addFollower:" + r.getStatus());

        return 0;
    }

    private int clt_removeFollower(String user,String userSub,String secret) {
        Response r = target.path("/update/subs/" + user + "/" + userSub)
                .queryParam(FeedsService.SECRET, secret).request()
                .delete();

        if( r.getStatus() != Status.OK.getStatusCode())
            System.out.println("Error, HTTP error status " + r.getStatus());

        return 0;
    }

    private int clt_deleteUser(String user, String secret) {
        Response r = target.path("/delete/" + user )
                .queryParam(FeedsService.SECRET, secret).request()
                .delete();

        if( r.getStatus() != Status.OK.getStatusCode() || !r.hasEntity())
            System.out.println("Error, HTTP error status " + r.getStatus());

        return 0;
    }

    @Override
    public long postMessage(String user, String pwd, Message msg) {
        return super.reTry( () -> clt_postMessage(user,pwd,msg) );
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {

    }

    @Override
    public Message getMessage(String user, long mid) {
        return super.reTry( () -> clt_getMessage(user,mid));
    }

    @Override
    public List<Message> getMessages(String user, long time) {
        return super.reTry( () -> clt_getMessages(user,time));
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

    @Override
    public void updateFeeds(Message msg, String user,String secret) {
        super.reTry( () -> clt_updateFeeds(msg,user,secret));
    }

    @Override
    public void addFollower(String user, String userSub, String secret) {
        super.reTry( () -> clt_addFollower(user, userSub,secret));
    }

    @Override
    public void removeFollower(String user, String subUser,String secret) {
        super.reTry( () -> clt_removeFollower(user,subUser,secret));
    }

    @Override
    public void deleteUser(String user, String secret) {
        super.reTry( () -> clt_deleteUser(user,secret));
    }

}
