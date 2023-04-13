package sd2223.trab1.server.resources;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import sd2223.trab1.api.rest.FeedsService;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.multicast.Discovery;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.clients.users.CheckUserClient;
import sd2223.trab1.server.UsersServer;

//IMPLEMENTAR PROPAGATE POST
//REFACTOR
@Singleton
public class FeedsResources implements FeedsService {

	// VER MELHOR
	private String domain;
	private String domainUsersURI;
	private long messageIdAssigner;
	private final Map<String, Map<Long, Message>> feeds = new HashMap<String, Map<Long, Message>>();
	private final Map<String, Map<Long, Message>> subscribedFeeds = new HashMap<String, Map<Long, Message>>();
	private final Map<String, List<String>> subscribed = new HashMap<String, List<String>>();
	private static Logger Log = Logger.getLogger(UsersResources.class.getName());

//	public FeedsResources(String domain, int idBeginner) {
//		this.domain = domain;
//		this.messageIdAssigner = idBeginner;
////		Discovery client = Discovery.getInstance();
////		String[] domainserviceURIs = client.knownUrisOf(domain);
////		boolean found = false;
////		int i = 0;
////		while (!found) {
////			String uris = domainserviceURIs[i];
////			if (uris.contains("users")) {
////				found = true;
////				String[] uriSplitted = uris.split(" ");
////				domainUsersURI = uriSplitted[1];
////			} else
////				i++;
////		}
//	}

	@Override
	public long postMessage(String userANDdomain, String pwd, Message msg) {
		if (userANDdomain == null || pwd == null || msg == null) {
			Log.info("There's information missing!");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		if (!userANDdomain.contains(domain)) {
			Log.info("Not the user's domain");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		try {
			String username = userANDdomain.split("@")[0];
			String[] args = new String[] { domainUsersURI, username, pwd };
			CheckUserClient.main(args);
		} catch (WebApplicationException e) {
			throw new WebApplicationException(Status.FORBIDDEN);
		} catch (IOException e) {
			e.printStackTrace();
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
		propagatePost(message);
		return msg.getId();

	}

	public void removeFromPersonalFeed(String userANDdomain, long mid, String pwd) {
		if (userANDdomain == null || pwd == null || mid == -1) {
			Log.info("Null information was given");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		if (!userANDdomain.contains(domain)) {
			Log.info("Not the user's domain");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		try {
			String username = userANDdomain.split("@")[0];
			String[] args = new String[] { domainUsersURI, username, pwd };
			CheckUserClient.main(args);
		} catch (WebApplicationException e) {
			throw new WebApplicationException(Status.FORBIDDEN);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (feeds.get(userANDdomain).remove(mid) == null) {
			Log.info("The message with the given ID does not exist");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		// propagateDelete()
	}

	// VER MELHOR
	@Override
	public Message getMessage(String userANDdomain, long mid) {
		if (userANDdomain == null || mid == -1) {
			Log.info("Null information was given");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		if (!userANDdomain.contains(domain)) {
			Log.info("Not the user's domain");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		Map<Long, Message> personalMessages = feeds.get(userANDdomain);
		if (personalMessages == null) {
			Map<Long, Message> subscribedMessages = subscribedFeeds.get(userANDdomain);
			if (subscribedMessages == null) {
				Log.info("Message not found");
				throw new WebApplicationException(Status.NOT_FOUND);
			} else {
				Message message = subscribedMessages.get(mid);
				if (message == null) {
					Log.info("Message not found");
					throw new WebApplicationException(Status.NOT_FOUND);
				}
				return message;
			}
		} else {
			Message message = personalMessages.get(mid);
			if (message == null) {
				Log.info("Message not found");
				throw new WebApplicationException(Status.NOT_FOUND);
			}
			return message;
		}
	}

	@Override
	public List<Message> getMessages(String userANDdomain, long time) {
		if (userANDdomain == null || time == -1) {
			Log.info("Null information was given");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		if (!userANDdomain.contains(domain)) {
			Log.info("Not the user's domain");
			throw new WebApplicationException(Status.NOT_FOUND);
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
		return toList;
	}

	@Override
	public void subUser(String userANDdomain, String userSub, String pwd) {
		if (userANDdomain == null || userSub == null || pwd == null) {
			Log.info("There's information missing!");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		if (!userANDdomain.contains(domain)) {
			Log.info("Not the user's domain");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		try {
			String username = userANDdomain.split("@")[0];
			String[] args = new String[] { domainUsersURI, username, pwd };
			CheckUserClient.main(args);
		} catch (WebApplicationException e) {
			throw new WebApplicationException(Status.FORBIDDEN);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!subscribed.containsKey(userANDdomain)) {
			List<String> users = new ArrayList<String>();
			users.add(userSub);
			subscribed.put(userANDdomain, users);
		} else {
			List<String> users = subscribed.get(userANDdomain);
			users.add(userSub);
			subscribed.put(userANDdomain, users);
		}
	}

	@Override
	public void unsubscribeUser(String userANDdomain, String userSub, String pwd) {
//		if (userANDdomain == null || userSub == null || pwd == null) {
//			Log.info("There's information missing!");
//			throw new WebApplicationException(Status.BAD_REQUEST);
//		}
//		if (!userANDdomain.contains(domain)) {
//			Log.info("Not the user's domain");
//			throw new WebApplicationException(Status.NOT_FOUND);
//		}
//		try {
//			// fazer check do user
//			// remoteUsers.getUser(username, pwd);
//		} catch (WebApplicationException e) {
//			throw new WebApplicationException(Status.FORBIDDEN);
//		}
//		if (!subscribed.containsKey(userANDdomain)) {
//			List<String> users = new ArrayList<String>();
//			users.add(userSub);
//			subscribed.put(userANDdomain, users);
//		} else {
//			List<String> users = subscribed.get(userANDdomain);
//			users.add(userSub);
//			subscribed.put(userANDdomain, users);
//		}
	}

	@Override
	public List<String> listSubs(String userANDdomain) {
//		if (userANDdomain == null) {
//			Log.info("There's information missing!");
//			throw new WebApplicationException(Status.BAD_REQUEST);
//		}
//		if (!userANDdomain.contains(domain)) {
//			Log.info("Not the user's domain");
//			throw new WebApplicationException(Status.NOT_FOUND);
//		}
//		List<String> toList = new ArrayList<String>();
//		if (!subscribed.containsKey(userANDdomain)) {
//			return toList;
//		} else {
//			List<String> users = subscribed.get(userANDdomain);
//			users.add(userSub);
//			subscribed.put(userANDdomain, users);
//		}
		return null;
	}

	private void propagatePost(Message message) {

	}

}
