package sd2223.trab1.server.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

public class JavaFeeds implements Feeds {

	private long messageIdAssigner = 0;
	private final Map<String, Map<Long, Message>> feeds = new ConcurrentHashMap<String, Map<Long, Message>>();
	private final Map<String, Map<Long, Message>> subscribedFeeds = new ConcurrentHashMap<String, Map<Long, Message>>();
	private final Map<String, Set<String>> subscribers = new ConcurrentHashMap<String, Set<String>>();
	private final Map<String, Set<String>> subscribed = new ConcurrentHashMap<String, Set<String>>();
	private Users usersClient;
	private static Logger Log = Logger.getLogger(JavaFeeds.class.getName());

	@Override
	public Result<Long> postMessage(String userANDdomain, String pwd, Message msg) {
		Log.info("postMessage : user = " + userANDdomain + "; pwd = " + pwd + "; msg = " + msg);
		String username = userANDdomain.split("@")[0];
		String userDomain = userANDdomain.split("@")[1];

		if (userANDdomain == null || pwd == null || msg == null || !userDomain.equals(msg.getDomain())) {
			Log.info("There's information missing! A given object is null.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		var r = checkUser(username, pwd, Domain.domain, true);
		if (!r.isOK())
			return Result.error(r.error());

		var id = generateID();
		Message message = new Message(id, username, msg.getDomain(), msg.getText());

		Map<Long, Message> messages;
		synchronized (feeds) {
			if (!feeds.containsKey(userANDdomain))
				messages = new HashMap<Long, Message>();
			else
				messages = feeds.get(userANDdomain);

			messages.put(message.getId(), message);
			feeds.put(userANDdomain, messages);
			propagatePost(userANDdomain, message);

			return Result.ok(id);
		}
	}

	@Override
	public Result<Void> postSubMessage(String userANDdomain, Message msg) {
		Log.info("postSubMessage : user = " + userANDdomain + "; msg = " + msg);
		if (userANDdomain == null || msg == null) {
			Log.info("A null username or message was received");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		if (!userANDdomain.contains(Domain.domain)) {
			Log.info("This is not the right domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		Map<Long, Message> messages;
		synchronized (subscribedFeeds) {
			if (!subscribedFeeds.containsKey(userANDdomain))
				messages = new HashMap<Long, Message>();
			else
				messages = subscribedFeeds.get(userANDdomain);

			messages.put(msg.getId(), msg);
			subscribedFeeds.put(userANDdomain, messages);
			return Result.ok();
		}
	}

	@Override
	public Result<Void> removeFromPersonalFeed(String userANDdomain, long mid, String pwd) {
		Log.info("removeFromPersonalFeed : user = " + userANDdomain + "; pwd = " + pwd + "; mid = " + mid);
		String username = userANDdomain.split("@")[0];

		if (pwd == null) {
			Log.info("Null information was given. Insert password.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		var r = checkUser(username, pwd, Domain.domain, true);
		if (!r.isOK())
			return Result.error(r.error());

		synchronized (feeds) {
			if (feeds.containsKey(userANDdomain)) {
				var feed = feeds.get(userANDdomain);
				var msg = feeds.get(userANDdomain).get(mid);

				if (msg != null) {
					feed.remove(mid);
					propagateDelete(userANDdomain, msg.getId());
					return Result.ok();
				} else {
					Log.info("The message with the given ID does not exist");
					return Result.error(ErrorCode.NOT_FOUND);
				}
			} else {
				return Result.error(ErrorCode.NOT_FOUND);
			}
		}
	}

	@Override
	public Result<Void> removeFromSubscribedFeed(String userANDdomain, long mid) {
		Log.info("removeFromSubscribedFeed : user = " + userANDdomain + "; mid = " + mid);
		if (userANDdomain == null || mid == -1) {
			Log.info("A null username or message was received");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		if (!userANDdomain.contains(Domain.domain)) {
			Log.info("This is not the right domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		synchronized (subscribedFeeds) {
			subscribedFeeds.get(userANDdomain).remove(mid);
			return Result.ok();
		}
	}

	@Override
	public Result<Void> removeFeed(String userANDdomain) {
		Log.info("removeFeed : user = " + userANDdomain);
		if (userANDdomain == null) {
			Log.info("Null information was given");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		if (!userANDdomain.contains(Domain.domain)) {
			Log.info("Not the user's domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		performRemoves(userANDdomain);
		return Result.ok();
	}

	@Override
	public Result<Void> removeFromSubscribed(String userANDdomain, String sub) {
		Log.info("removeFromSubscribed : user = " + userANDdomain + "; sub = " + sub);
		if (userANDdomain == null || sub == null) {
			Log.info("There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		if (!userANDdomain.contains(Domain.domain)) {
			Log.info("This is not the right domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		synchronized (subscribed) {
			subscribed.get(userANDdomain).remove(sub);
			return Result.ok();
		}
	}

	@Override
	public Result<Message> getMessage(String userANDdomain, long mid) {
		Log.info("getMessage : user = " + userANDdomain + "; mid = " + mid);
		String username = userANDdomain.split("@")[0];
		String userDomain = userANDdomain.split("@")[1];

		var r = checkUser(username, null, userDomain, false);
		if (!r.isOK())
			return Result.error(r.error());

		if (!userDomain.equals(Domain.domain))
				return FeedsClientFactory.getFeedsClient(userDomain).getMessage(userANDdomain, mid);
		else {
			return getMessageOnThisDomain(userANDdomain, mid);
		}
	}

	@Override
	public Result<List<Message>> getMessages(String userANDdomain, long time) {
		Log.info("getMessages : user = " + userANDdomain + "; mid = " + time);
		var username = userANDdomain.split("@")[0];
		var userDomain = userANDdomain.split("@")[1];

		var r = checkUser(username, null, userDomain, false);
		if (!r.isOK())
			return Result.error(r.error());

		if (!userDomain.equals(Domain.domain))
			return FeedsClientFactory.getFeedsClient(userDomain).getMessages(userANDdomain, time);
		else
			return Result.ok(performListing(userANDdomain, time));
	}

	@Override
	public Result<Void> subUser(String userANDdomain, String userSub, String pwd) {
		Log.info("subUser : user = " + userANDdomain + "; userSub = " + userSub + "; pwd = " + pwd);
		String username = userANDdomain.split("@")[0];
		String userSubName = userSub.split("@")[0];
		String userSubDomain = userSub.split("@")[1];

		if (userANDdomain == null || userSub == null || pwd == null) {
			Log.info("There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		var r1 = checkUser(username, null, Domain.domain, false);
		var r2 = checkUser(userSubName, null, userSubDomain, false);
		if (!r1.isOK() || !r2.isOK()) {
			Log.info("One of the users does not exist");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		var r3 = UsersClientFactory.getUsersClient(Domain.domain).verifyPassword(username, pwd);
		if (!r3.isOK()) {
			Log.info("The password is incorrect");
			return Result.error(ErrorCode.FORBIDDEN);
		}

		synchronized (subscribed) {
			if (!subscribed.containsKey(userANDdomain)) {
				Set<String> subs = new HashSet<String>();
				subs.add(userSub);
				subscribed.put(userANDdomain, subs);
			} else
				subscribed.get(userANDdomain).add(userSub);

			FeedsClientFactory.getFeedsClient(userSubDomain).addSubscriber(userSub, userANDdomain);
			return Result.ok();
		}
	}

	@Override
	public Result<Void> addSubscriber(String userANDdomain, String sub) {
		Log.info("addSubscriber : user = " + userANDdomain + "; sub = " + sub);
		if (userANDdomain == null || sub == null) {
			Log.info("There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		if (!userANDdomain.contains(Domain.domain)) {
			Log.info("This is not the right domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		synchronized (subscribers) {
			if (!subscribers.containsKey(userANDdomain)) {
				Set<String> subs = new HashSet<String>();
				subs.add(sub);
				subscribers.put(userANDdomain, subs);
			} else
				subscribers.get(userANDdomain).add(sub);

			return Result.ok();
		}
	}

	@Override
	public Result<Void> unsubscribeUser(String userANDdomain, String userSub, String pwd) {
		Log.info("unsubscribeUser : user = " + userANDdomain + "; userSub = " + userSub + "; pwd = " + pwd);
		String username = userANDdomain.split("@")[0];
		String userSubName = userSub.split("@")[0];
		String userSubDomain = userSub.split("@")[1];

		if (userANDdomain == null || userSub == null || pwd == null) {
			Log.info("There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		var r1 = checkUser(username, null, Domain.domain, false);
		var r2 = checkUser(userSubName, null, userSubDomain, false);
		if (!r1.isOK() || !r2.isOK()) {
			Log.info("One of the users does not exist");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		var r3 = UsersClientFactory.getUsersClient(Domain.domain).verifyPassword(username, pwd);
		if (!r3.isOK()) {
			Log.info("The password is incorrect");
			return Result.error(ErrorCode.FORBIDDEN);
		}

		synchronized (subscribed) {
			if (subscribed.containsKey(userANDdomain))
				subscribed.get(userANDdomain).remove(userSub);

			FeedsClientFactory.getFeedsClient(userSubDomain).removeSubscriber(userSub, userANDdomain);
			return Result.ok();
		}
	}

	@Override
	public Result<Void> removeSubscriber(String userANDdomain, String sub) {
		Log.info("removeSubscriber : user = " + userANDdomain + "; userSub = " + sub);
		if (userANDdomain == null || sub == null) {
			Log.info("There's information missing!");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		if (!userANDdomain.contains(Domain.domain)) {
			Log.info("This is not the right domain");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		synchronized (subscribers) {
			if (subscribers.containsKey(userANDdomain))
				subscribers.get(userANDdomain).remove(sub);
			return Result.ok();
		}
	}

	@Override
	public Result<List<String>> listSubs(String userANDdomain) {
		Log.info("listSubs : user = " + userANDdomain);
		String username = userANDdomain.split("@")[0];

		var r = checkUser(username, null, Domain.domain, false);
		if (!r.isOK())
			return Result.error(r.error());

		List<String> toList = new ArrayList<String>();
		synchronized (subscribed) {
			if (!subscribed.containsKey(userANDdomain)) {
				Log.info("The user does not subscribe anyone");
				return Result.ok(toList);
			}

			toList.addAll(subscribed.get(userANDdomain));
			return Result.ok(toList);
		}
	}

	private void propagatePost(String userANDdomain, Message message) {
		var userSubs = subscribers.get(userANDdomain);
		if (userSubs != null) {
			for (var sub : userSubs) {
				String domain = sub.split("@")[1];
				new Thread(() -> {
					FeedsClientFactory.getFeedsClient(domain).postSubMessage(sub, message);
				}).start();
			}
		}
	}

	private void propagateDelete(String userANDdomain, long mid) {
		var userSubs = subscribers.get(userANDdomain);
		if (userSubs != null) {
			for (var sub : userSubs) {
				String domain = sub.split("@")[1];
				new Thread(() -> {
					FeedsClientFactory.getFeedsClient(domain).removeFromSubscribedFeed(sub, mid);
				}).start();
			}
		}
	}

	private void propagateDeleteSubs(String userANDdomain) {
		var subsList = subscribers.get(userANDdomain);
		if (subsList != null) {
			for (var sub : subsList) {
				String domain = sub.split("@")[1];
				new Thread(() -> {
					FeedsClientFactory.getFeedsClient(domain).removeFromSubscribed(sub, userANDdomain);
				}).start();
			}
		}
	}

	private Result<Void> checkUser(String username, String pwd, String domain, boolean alsoVerify) {
		usersClient = UsersClientFactory.getUsersClient(domain);

		var response = usersClient.checkUser(username);
		if (!response.isOK()) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		if (alsoVerify) {
			response = usersClient.verifyPassword(username, pwd);
			if (!response.isOK()) {
				Log.info("The password is incorrect");
				return Result.error(ErrorCode.FORBIDDEN);
			}
		}
		return Result.ok();
	}

	private synchronized Result<Message> getMessageOnThisDomain(String userANDdomain, long mid) {
		if (feeds.get(userANDdomain) == null || !feeds.get(userANDdomain).containsKey(mid)) {
			if (subscribedFeeds.get(userANDdomain) == null || !subscribedFeeds.get(userANDdomain).containsKey(mid)) {
				Log.info("Message not found");
				return Result.error(ErrorCode.NOT_FOUND);
			} else {
				Message message = subscribedFeeds.get(userANDdomain).get(mid);
				if (message == null) {
					Log.info("Message not found");
					return Result.error(ErrorCode.NOT_FOUND);
				}
				return Result.ok(message);
			}
		} else {
			Message message = feeds.get(userANDdomain).get(mid);
			if (message == null) {
				Log.info("Message not found");
				return Result.error(ErrorCode.NOT_FOUND);
			}
			return Result.ok(message);
		}
	}

	private synchronized void performRemoves(String userANDdomain) {
		feeds.remove(userANDdomain);
		subscribedFeeds.remove(userANDdomain);
		subscribed.remove(userANDdomain);
		propagateDeleteSubs(userANDdomain);
		subscribers.remove(userANDdomain);
	}

	private synchronized List<Message> performListing(String userANDdomain, long time) {
		List<Message> toList = new ArrayList<Message>();
		if (feeds.containsKey(userANDdomain)) {
			Collection<Message> messages = feeds.get(userANDdomain).values();
			if (messages != null) {
				for (Message msg : messages) {
					if (msg.getCreationTime() > time || time == 0)
						toList.add(msg);
				}
			}
		}

		if (subscribedFeeds.containsKey(userANDdomain)) {
			Collection<Message> messages = subscribedFeeds.get(userANDdomain).values();
			if (messages != null) {
				for (Message msg : messages) {
					if (msg.getCreationTime() > time || time == 0)
						toList.add(msg);
				}
			}
		}
		return toList;
	}

	private long generateID() {
		this.messageIdAssigner++;
		return messageIdAssigner * 256 + Domain.id;
	}

}