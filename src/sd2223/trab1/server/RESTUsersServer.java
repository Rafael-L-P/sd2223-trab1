package sd2223.trab1.server;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd2223.trab1.api.Discovery;
import sd2223.trab1.server.resources.FeedResource;
import sd2223.trab1.server.resources.UsersResource;

public class RESTUsersServer {

    private static Logger Log = Logger.getLogger(RESTUsersServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "users";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";
    private static String domain;
    private static int serverID;

    public static void main(String[] args) {

        domain = args[0];

        try {

            ResourceConfig config = new ResourceConfig();
            config.register(UsersResource.class);
            //config.register(CustomLoggingFilter.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            String serviceName = domain + ":" + SERVICE;
            Discovery disc = Discovery.getInstance();
            disc.announce(serviceName, serverURI);

            Log.info(String.format("%s Server ready @ %s\n", serviceName, serverURI));

            // More code can be executed here...
        } catch (Exception e) {
            Log.severe(e.getMessage());
        }
    }
}
