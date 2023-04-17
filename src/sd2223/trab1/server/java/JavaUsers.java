package sd2223.trab1.server.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.java.Result.ErrorCode;;

public class JavaUsers implements Users {
	private final Map<String, User> users = new HashMap<>();
	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);
		if (user.getName() == null || user.getPwd() == null || user.getDisplayName() == null
				|| user.getDomain() == null) {
			Log.info("User object invalid. There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		if (users.putIfAbsent(user.getName(), user) != null) {
			Log.info("User already exists.");
			return Result.error(ErrorCode.CONFLICT);
		}
		return Result.ok(createString(user));
	}

	@Override
	public Result<User> getUser(String name, String pwd) {
		Log.info("getUser : user = " + name + "; pwd = " + pwd);
		if (name == null || pwd == null) {
			Log.info("UserId or password null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		var user = users.get(name);
		if (user == null) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		if (!user.getPwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}
		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String name, String pwd, User user) {
		Log.info("updateUser : user = " + name + "; pwd = " + pwd + " ; user = " + user);
		if (name == null || pwd == null) {
			Log.info("There's missing information!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		if (!user.getName().equals(name)) {
			Log.info("Name does not match!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		var existingUser = users.get(name);
		if (existingUser == null) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		if (!existingUser.getPwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}
		existingUser = modifyUser(existingUser, user);
		users.put(name, existingUser);
		return Result.ok(existingUser);
	}

	@Override
	public Result<Void> verifyPassword(String name, String pwd) {
		var res = getUser(name, pwd);
		if (res.isOK())
			return Result.ok();
		else
			return Result.error(res.error());
	}

	@Override
	public Result<User> deleteUser(String name, String pwd) {
		Log.info("deleteUser : user = " + name + "; pwd = " + pwd);
		if (name == null || pwd == null) {
			Log.info("UserId or password null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		var user = users.get(name);
		if (user == null) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		if (!user.getPwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}
		deleteFeed(user);
		users.remove(name);
		return Result.ok(user);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		if (pattern == null) {
			Log.info("UserId or password null.");
			return Result.error(ErrorCode.BAD_REQUEST);
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
		return Result.ok(toList);
	}

	private User modifyUser(User existingUser, User user) {
		if (user != null) {
			if (user.getDisplayName() != null)
				existingUser.setDisplayName(user.getDisplayName());
			if (user.getDomain() != null)
				existingUser.setDomain(user.getDomain());
			if (user.getPwd() != null)
				existingUser.setPwd(user.getPwd());
		}
		return existingUser;
	}

	private void deleteFeed(User user) {

	}

	private String createString(User user) {
		return user.getName() + "@" + user.getDomain();
	}

}
