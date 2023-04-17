package sd2223.trab1.server.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.FeedsClientFactory;
import sd2223.trab1.clients.UsersClientFactory;
import sd2223.trab1.multicast.Domain;
import sd2223.trab1.api.java.Result.ErrorCode;;

//VER SE DEVO FAZER O PROPAGATE DELETE
public class JavaFeeds implements Feeds {

	// VER MELHOR
	private final String domain = Domain.get();
	private long messageIdAssigner;
	private final Map<String, Map<Long, Message>> feeds = new HashMap<String, Map<Long, Message>>();
	private final Map<String, Map<Long, Message>> subscribedFeeds = new HashMap<String, Map<Long, Message>>();
	private final Map<String, List<String>> subscribers = new HashMap<String, List<String>>();
	private static Logger Log = Logger.getLogger(JavaFeeds.class.getName());
	private final Users usersClient = UsersClientFactory.getUsersClient(domain);

	@Override
	public Result<Long> postMessage(String userANDdomain, String pwd, Message msg) {
		if (userANDdomain == null || pwd == null || msg == null) {
			Log.info("There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
//		if (!userANDdomain.contains(domain)) {
//			Log.info("Not the user's domain");
//			return Result.error(ErrorCode.NOT_FOUND);
//		}
		String username = userANDdomain.split("@")[0];
		var response = usersClient.checkUser(username);
		if (!response.isOK()) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		response = usersClient.verifyPassword(username, pwd);
		if (!response.isOK()) {
			Log.info("The password is incorrect");
			return Result.error(ErrorCode.FORBIDDEN);
		}
		Message message = new Message(this.messageIdAssigner, userANDdomain, msg.getDomain(), msg.getText());
		this.messageIdAssigner++;
		if (!feeds.containsKey(userANDdomain)) {
			Map<Long, Message> messages = new HashMap<Long, Message>();
			messages.put(message.getId(), message);
			feeds.put(userANDdomain, messages);
		} else {
			Map<Long, Message> messages = feeds.get(userANDdomain);
			messages.put(message.getId(), message);
			feeds.put(userANDdomain, messages);
		}
		propagatePost(userANDdomain, message);
		return Result.ok(msg.getId());
	}

	@Override
	public Result<Void> postSubMessage(String userANDdomain, Message msg) {
		if (msg == null || domain == null) {
			Log.info("A null message or domain identifier was received");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		if (!userANDdomain.contains(domain)) {
			Log.info("This is not the right domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		if (!subscribedFeeds.containsKey(userANDdomain)) {
			Map<Long, Message> messages = new HashMap<Long, Message>();
			messages.put(msg.getId(), msg);
			subscribedFeeds.put(userANDdomain, messages);
		} else {
			Map<Long, Message> messages = subscribedFeeds.get(userANDdomain);
			messages.put(msg.getId(), msg);
			subscribedFeeds.put(userANDdomain, messages);
		}
		return Result.ok();
	}

	@Override
	public Result<Void> removeFromPersonalFeed(String userANDdomain, long mid, String pwd) {
		if (userANDdomain == null || pwd == null || mid == -1) {
			Log.info("Null information was given");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
//		if (!userANDdomain.contains(domain)) {
//			Log.info("Not the user's domain");
//			return Result.error(ErrorCode.NOT_FOUND);
//		}
		String username = userANDdomain.split("@")[0];
		var response = usersClient.checkUser(username);
		if (!response.isOK()) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		response = usersClient.verifyPassword(username, pwd);
		if (!response.isOK()) {
			Log.info("The password is incorrect");
			return Result.error(ErrorCode.FORBIDDEN);
		}
		if (feeds.get(userANDdomain).remove(mid) == null) {
			Log.info("The message with the given ID does not exist");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		// propagateDelete()
		return Result.ok();
	}
	
	@Override
	public Result<Void> removeFeed(String userANDdomain) {
		if (userANDdomain == null) {
			Log.info("Null information was given");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		if (!userANDdomain.contains(domain)) {
			Log.info("Not the user's domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		feeds.remove(userANDdomain);
		return Result.ok();
	}

	// VER MELHOR
	@Override
	public Result<Message> getMessage(String userANDdomain, long mid) {
		if (userANDdomain == null || mid == -1) {
			Log.info("Null information was given");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
//		if (!userANDdomain.contains(domain)) {
//			Log.info("Not the user's domain");
//			return Result.error(ErrorCode.NOT_FOUND);
//		}
		String username = userANDdomain.split("@")[0];
		var response = usersClient.checkUser(username);
		if (!response.isOK()) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		Map<Long, Message> personalMessages = feeds.get(userANDdomain);
		if (personalMessages == null) {
			Map<Long, Message> subscribedMessages = subscribedFeeds.get(userANDdomain);
			if (subscribedMessages == null) {
				Log.info("Message not found");
				return Result.error(ErrorCode.NOT_FOUND);
			} else {
				Message message = subscribedMessages.get(mid);
				if (message == null) {
					Log.info("Message not found");
					return Result.error(ErrorCode.NOT_FOUND);
				}
				return Result.ok(message);
			}
		} else {
			Message message = personalMessages.get(mid);
			if (message == null) {
				Log.info("Message not found");
				return Result.error(ErrorCode.NOT_FOUND);
			}
			return Result.ok(message);
		}
	}

	@Override
	public Result<List<Message>> getMessages(String userANDdomain, long time) {
		if (userANDdomain == null || time == -1) {
			Log.info("Null information was given");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		if (!userANDdomain.contains(domain)) {
			Log.info("Not the user's domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		String username = userANDdomain.split("@")[0];
		var response = usersClient.checkUser(username);
		if (!response.isOK()) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		List<Message> toList = new ArrayList<Message>();
		Collection<Message> messages = feeds.get(userANDdomain).values();
		if (messages != null) {
			Iterator<Message> it = messages.iterator();
			while (it.hasNext()) {
				Message itMessage = it.next();
				if (itMessage.getCreationTime() > time)
					toList.add(itMessage);
			}
		}
		messages = subscribedFeeds.get(userANDdomain).values();
		if (messages != null) {
			Iterator<Message> it = messages.iterator();
			while (it.hasNext()) {
				Message itMessage = it.next();
				if (itMessage.getCreationTime() > time)
					toList.add(itMessage);
			}
		}
		return Result.ok(toList);
	}

	@Override
	public Result<Void> subUser(String userANDdomain, String userSub, String pwd) {
		if (userANDdomain == null || userSub == null || pwd == null) {
			Log.info("There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		String username = userANDdomain.split("@")[0];
		var response = usersClient.checkUser(username);
		if (!response.isOK()) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		String userSubName = userSub.split("@")[0];
		String userSubDomain = userSub.split("@")[1];
		response = UsersClientFactory.getUsersClient(userSubDomain).checkUser(userSubName);
		if (!response.isOK()) {
			Log.info("User to sub does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		response = usersClient.verifyPassword(username, pwd);
		if (!response.isOK()) {
			Log.info("The password is incorrect");
			return Result.error(ErrorCode.FORBIDDEN);
		}
		FeedsClientFactory.getFeedsClient(userSubDomain).addSubscriber(userSubName, userANDdomain);
		return Result.ok();
	}

	public Result<Void> addSubscriber(String userANDdomain, String sub) {
		if (userANDdomain == null || sub == null) {
			Log.info("There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		if (!userANDdomain.contains(domain)) {
			Log.info("This is not the right domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		if (!subscribers.containsKey(userANDdomain)) {
			List<String> subs = new ArrayList<String>();
			subs.add(sub);
			subscribers.put(userANDdomain, subs);
		} else {
			List<String> subs = subscribers.get(userANDdomain);
			subs.add(sub);
			subscribers.put(userANDdomain, subs);
		}
		return Result.ok();
	}

	@Override
	public Result<Void> unsubscribeUser(String userANDdomain, String userSub, String pwd) {
		if (userANDdomain == null || userSub == null || pwd == null) {
			Log.info("There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		String username = userANDdomain.split("@")[0];
		var response = usersClient.checkUser(username);
		if (!response.isOK()) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		String userSubName = userSub.split("@")[0];
		String userSubDomain = userSub.split("@")[1];
		response = UsersClientFactory.getUsersClient(userSubDomain).checkUser(userSubName);
		if (!response.isOK()) {
			Log.info("User to sub does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		response = usersClient.verifyPassword(username, pwd);
		if (!response.isOK()) {
			Log.info("The password is incorrect");
			return Result.error(ErrorCode.FORBIDDEN);
		}
		FeedsClientFactory.getFeedsClient(userSubDomain).removeSubscriber(userSubName, userANDdomain);
		return Result.ok();
	}

	public Result<Void> removeSubscriber(String userANDdomain, String sub) {
		if (userANDdomain == null || sub == null) {
			Log.info("There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}
		if (!userANDdomain.contains(domain)) {
			Log.info("This is not the right domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		subscribers.get(userANDdomain).remove(sub);
		return Result.ok();
	}

	@Override
	public Result<List<String>> listSubs(String userANDdomain) {
		if (userANDdomain == null) {
			Log.info("There's information missing!");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
//			if (!userANDdomain.contains(domain)) {
//				Log.info("Not the user's domain");
//				throw new WebApplicationException(Status.NOT_FOUND);
//			}
		String username = userANDdomain.split("@")[0];
		var response = usersClient.checkUser(username);
		if (!response.isOK()) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}
		List<String> toList = new ArrayList<String>();
		if (!subscribers.containsKey(userANDdomain))
			return Result.ok(toList);
		return Result.ok(subscribers.get(userANDdomain));
	}

	private void propagatePost(String userANDdomain, Message message) {
		List<String> userSubs = subscribers.get(userANDdomain);
		if (userSubs != null) {
			for (var sub : userSubs) {
				String domain = sub.split("@")[1];
				FeedsClientFactory.getFeedsClient(domain).postSubMessage(sub, message);
			}
		}
	}

}
