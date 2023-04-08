package sd2223.trab1.server.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import sd2223.trab1.api.Feed;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

@Singleton
public class UsersResource implements UsersService, FeedsService {

	private final Map<String,User> users = new HashMap<>();
	private final Map<String, Feed> feeds = new HashMap<>();
	private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	public UsersResource() {
	}

	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);
		
		// Check if user data is valid
		if(user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
			Log.info("User object invalid.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}
		
		// Insert user, checking if name already exists
		if( users.putIfAbsent(user.getName(), user) != null ) {
			Log.info("User already exists.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		return user.getName();
	}
	
	@Override
	public User getUser(String name, String pwd) {
			Log.info("getUser : user = " + name + "; pwd = " + pwd);
			
			// Check if user is valid
			if(name == null || pwd == null) {
				Log.info("Name or Password null.");
				throw new WebApplicationException( Status.BAD_REQUEST );
			}
			
			User user = users.get(name);			
			// Check if user exists 
			if( user == null ) {
				Log.info("User does not exist.");
				throw new WebApplicationException( Status.NOT_FOUND );
			}
			
			//Check if the password is correct
			if( !user.getPwd().equals( pwd)) {
				Log.info("Password is incorrect.");
				throw new WebApplicationException( Status.FORBIDDEN );
			}
			
			return user;
		}

	@Override
	public User updateUser(String name, String pwd, User user) {
		Log.info("updateUser : user = " + name + "; pwd = " + pwd + " ; user = " + user);

		// Check if user is valid
		if(name == null || pwd == null) {
			Log.info("Name or password null.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}

		var currentUser = users.get(name);

		// Check if user exists
		if( currentUser == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}

		//Check if the password is correct
		if( !currentUser.getPwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}

		currentUser.setPwd(user.getPwd());
		currentUser.setDomain(user.getDomain());
		currentUser.setDisplayName(user.getDisplayName());
		currentUser.setName(user.getName());

		return currentUser;
	}

	@Override
	public User deleteUser(String name, String pwd) {
		Log.info("deleteUser : user = " + name + "; pwd = " + pwd);

		var currentUser = users.get(name);

		// Check if user exists
		if( currentUser == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}

		//Check if the password is correct
		if( !currentUser.getPwd().equals( pwd )) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}

		users.remove(name);

		return currentUser;
	}

	@Override
	public List<User> searchUsers(String pattern) {
		List<User> result = new ArrayList<User>();
		users.values().stream().forEach( u -> { if( u.getDisplayName().
				indexOf(pattern) != -1) result.add( new User(u.getName(),u.getPwd(),u.getDomain(),u.getDisplayName())); });
		return result;
	}

	// Feed service
	@Override
	public long postMessage(String user, String pwd, Message msg) {
		// Check if user is valid
		if(user == null || pwd == null || msg == null) {
			Log.info("User, password or Message null.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}

		var currentUser = users.get(user);

		// Check if user exists
		if( currentUser == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}

		//Check if the password is correct
		if( !currentUser.getPwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}

		var feed = feeds.get(user);

		//long mid = ;

		feed.postMessage(msg);

		//Todo
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
