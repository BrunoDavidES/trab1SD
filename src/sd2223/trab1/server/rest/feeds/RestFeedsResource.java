package sd2223.trab1.server.rest.feeds;

import java.net.URI;
import java.util.List;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.clients.UsersClientFactory;
import sd2223.trab1.multicast.Discovery;
import sd2223.trab1.multicast.Domain;
import sd2223.trab1.server.java.JavaFeeds;
import sd2223.trab1.server.java.JavaUsers;
import sd2223.trab1.server.rest.RestResource;
import jakarta.inject.Singleton;

@Singleton
public class RestFeedsResource extends RestResource implements FeedsService {

	final Feeds impl;
	//int id;
	
	public RestFeedsResource(String domain, int id) {
		this.impl = new JavaFeeds();
		Domain.set(domain);
	}
	@Override
	public long postMessage(String user, String pwd, Message msg) {
		return super.fromJavaResult(impl.postMessage(user, pwd, msg));
	}
	
	@Override
	public void removeFromPersonalFeed(String user, long mid, String pwd) {
		super.fromJavaResult(impl.removeFromPersonalFeed(user, mid, pwd));
	}
	
	@Override
	public Message getMessage(String user, long mid) {
		return super.fromJavaResult(impl.getMessage(user, mid));
	}
	
	@Override
	public List<Message> getMessages(String user, long time) {
		return super.fromJavaResult(impl.getMessages(user, time));
	}
	
	@Override
	public void subUser(String user, String userSub, String pwd) {
		super.fromJavaResult(impl.subUser(user, userSub, pwd));
	}
	
	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) {
		super.fromJavaResult(impl.unsubscribeUser(user, userSub, pwd));
	}
	
	@Override
	public List<String> listSubs(String user) {
		return super.fromJavaResult(impl.listSubs(user));
	}
		
}
