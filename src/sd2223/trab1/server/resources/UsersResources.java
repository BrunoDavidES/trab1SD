package sd2223.trab1.server.resources;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.multicast.Discovery;

//REFACTOR
//FALTA DELETE FEED
@Singleton
public class UsersResources implements UsersService {

	private String domain;
	private String domainUsersURI;
	private final Map<String, User> users = new HashMap<>();
	private static Logger Log = Logger.getLogger(UsersResources.class.getName());

	public UsersResources(String domain) {
		this.domain=domain;
		Discovery client = Discovery.getInstance();
		String[] domainserviceURIs = client.knownUrisOf(domain);
		boolean found = false;
		int i = 0;
		while (!found) {
			String uris = domainserviceURIs[i];
			if (uris.contains("feeds")) {
				found = true;
				String[] uriSplitted = uris.split(" ");
				domainUsersURI = uriSplitted[1];
			} else
				i++;
		}
	}

	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);
		if (user.getName() == null || user.getPwd() == null || user.getDisplayName() == null
				|| user.getDomain() == null) {
			Log.info("User object invalid. There's information missing!");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		if (users.putIfAbsent(user.getName(), user) != null) {
			Log.info("User already exists.");
			throw new WebApplicationException(Status.CONFLICT);
		}
		return createString(user);
	}

	@Override
	public User getUser(String name, String pwd) {
		Log.info("getUser : user = " + name + "; pwd = " + pwd);
		if (name == null || pwd == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		var user = users.get(name);
		if (user == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if (!user.getPwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		return user;
	}

	@Override
	public User updateUser(String name, String pwd, User user) {
		Log.info("updateUser : user = " + name + "; pwd = " + pwd + " ; user = " + user);
		if (name == null || pwd == null || user == null) {
			Log.info("There's missing information!");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		var existingUser = users.get(name);
		if (existingUser == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if (!existingUser.getPwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		existingUser = modifyUser(existingUser, user);
		users.put(name, existingUser);
		return existingUser;
	}

	@Override
	public boolean checkUser(String name, String pwd) {
		Log.info("checkUser : user = " + name);
		if (name == null || pwd == null) {
			Log.info("UserId null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		var user = users.get(name);
		if (user == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if (!user.getPwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		return true;
	}

	@Override
	public User deleteUser(String name, String pwd) {
		Log.info("deleteUser : user = " + name + "; pwd = " + pwd);
		if (name == null || pwd == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		var user = users.get(name);
		if (user == null) {
			Log.info("User does not exist.");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if (!user.getPwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		deleteFeed(user);
		users.remove(name);
		return user;
	}

	@Override
	public List<User> searchUsers(String pattern) {
		if (pattern == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		List<User> toList = new ArrayList<User>();
		Set<String> names = users.keySet();
		for (String uName : names) {
			if (uName.contains(pattern)) {
				User user = users.get(uName);
				user.setPwd("");
				toList.add(user);
			}
		}
		return toList;
	}

	private User modifyUser(User existingUser, User user) {
		if (user.getDisplayName() != null)
			existingUser.setDisplayName(user.getDisplayName());
		if (user.getDomain() != null)
			existingUser.setDomain(user.getDomain());
		if (user.getPwd() != null)
			existingUser.setPwd(user.getPwd());
		return existingUser;
	}

	private void deleteFeed(User user) {

	}

	private String createString(User user) {
		return user.getName() + "@" + user.getDomain();
	}

}
