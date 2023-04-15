package sd2223.trab1.server.rest.users;

import java.util.List;

import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.server.java.JavaUsers;
import sd2223.trab1.server.rest.RestResource;
import jakarta.inject.Singleton;

@Singleton
public class RestUsersResource extends RestResource implements UsersService {

	final Users impl;
	String domain;
	
	public RestUsersResource(String domain) {
		this.impl = new JavaUsers();
		this.domain=domain;
	}
	
	@Override
	public String createUser(User user) {
		return super.fromJavaResult( impl.createUser( user));
	}

	@Override
	public User getUser(String name, String pwd) {
		return super.fromJavaResult( impl.getUser(name, pwd));
	}

	@Override
	public void verifyPassword(String name, String pwd) {
		super.fromJavaResult( impl.verifyPassword(name, pwd));
	}
	
	@Override
	public User updateUser(String name, String pwd, User user) {
		return super.fromJavaResult( impl.updateUser(name, pwd, user));
	}

	@Override
	public User deleteUser(String name, String pwd) {
		return super.fromJavaResult( impl.deleteUser(name, pwd));
	}

	@Override
	public List<User> searchUsers(String pattern) {
		return super.fromJavaResult( impl.searchUsers(pattern));
	}
		
}
