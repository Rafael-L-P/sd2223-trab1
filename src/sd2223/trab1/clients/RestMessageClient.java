package sd2223.trab1.clients;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.rest.FeedsService;

import javax.print.attribute.standard.Media;
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

        /*Response r = target.path("sub/" + user + "/" + userSub)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity())
        */
    }

    private void clt_removeFromPersonalFeed(String user, long mid, String pwd) {

        Response r = target.path(user + "/" + mid)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        if( r.getStatus() != Status.OK.getStatusCode() || !r.hasEntity())
            System.out.println("Error, HTTP error status " + r.getStatus());
    }

    private void clt_propagateMessage(Message msg, String user) {

        Response r = target.path(msg + "/" + user)
                .request().accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(msg, MediaType.APPLICATION_JSON));

        if( r.getStatus() != Status.OK.getStatusCode() || !r.hasEntity())
            System.out.println("Error, HTTP error status " + r.getStatus());
    }

    @Override
    public long postMessage(String user, String pwd, Message msg) {
        return super.reTry( () -> clt_postMessage(user,pwd,msg) );
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {
        //super.reTry( () -> clt_removeFromPersonalFeed(user,mid,pwd));
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
        //super.reTry( () -> clt_subUser(user,userSub,pwd));
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {

    }

    @Override
    public List<String> listSubs(String user) {
        return null;
    }

    /*@Override
    public void updateFeeds(Message msg, String user) {

    }

    @Override
    public void propagateMessage(Message msg, String user) {
        //super.reTry( () -> clt_propagateMessage(msg,user));
    }
       */

}
