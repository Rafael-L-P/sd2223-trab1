package sd2223.trab1.server.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import sd2223.trab1.api.Discovery;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.UsersService;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.clients.RestUserServer;

@Singleton
public class UsersResource implements UsersService {
	private final static String USER_SERVICE = ":users";
	private final static String FEED_SERVICE = ":feeds";
	private final static String SECRET = "3Hc4q";
	private final Map<String, User> users = new ConcurrentHashMap<>();
	private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	private String domain;
	private Discovery dis;

	public UsersResource(String domain) {
		this.domain = domain;
		this.dis = Discovery.getInstance();
	}

	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);

		// Check if user data is valid
		if (user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
			Log.info("User object invalid.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// Insert user, checking if name already exists
		if (users.putIfAbsent(user.getName(), user) != null) {
			Log.info("User already exists.");
			throw new WebApplicationException(Status.CONFLICT);
		}
		String username = user.getName() + "@" + user.getDomain();
		return username;
	}

	@Override
	public User getUser(String name, String pwd) {
		Log.info("getUser : user = " + name + "; pwd = " + pwd);

		// Check if user is valid
		if (name == null || pwd == null) {
			Log.info("Name or Password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User user = users.get(name);
		// Check if user exists
		if (user == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		//Check if the password is correct
		if (!user.getPwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		return user;
	}

	@Override
	public User updateUser(String name, String pwd, User user) {
		Log.info("updateUser : user = " + name + "; pwd = " + pwd + " ; user = " + user);

		// Check if user is valid
		if (name == null || pwd == null) {
			Log.info("Name or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		if ( !name.equals(user.getName())  ) {
			Log.info("Tried to change the users name.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		var currentUser = users.get(name);

		// Check if user exists
		if (currentUser == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		//Check if the password is correct
		if (!currentUser.getPwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		String newPwd = user.getPwd();
		if(newPwd != null)
			currentUser.setPwd(newPwd);

		String displayName = user.getDisplayName();
		if (displayName != null)
			currentUser.setDisplayName(displayName);

		String domain = user.getDomain();
		if(domain != null) {
			currentUser.setDomain(domain);
		}

		return currentUser;
	}

	@Override
	public User deleteUser(String name, String pwd) {
		Log.info("deleteUser : user = " + name + "; pwd = " + pwd);

		if (name == null || pwd == null) {
			Log.info("Name or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		var currentUser = users.get(name);

		// Check if user exists
		if (currentUser == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		//Check if the password is correct
		if (!currentUser.getPwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		users.remove(name);
		deleteUserFeed(name);
		return currentUser;
	}

	@Override
	public List<User> searchUsers(String pattern) {
		List<User> result = new ArrayList<User>();
		users.values().stream().forEach(u -> {
			if (u.getName().
					indexOf(pattern) != -1)
				result.add(new User(u.getName(), u.getPwd(), u.getDomain(), u.getDisplayName()));
		});
		return result;
	}

	@Override
	public boolean hasUser(String name) {
		return users.containsKey(name);
	}

	public void deleteUserFeed(String user) {
		URI[] uri = dis.knownUrisOf(domain+FEED_SERVICE, 1);

		new RestUserServer(uri[0]).deleteUser(user,SECRET);
	}
}

