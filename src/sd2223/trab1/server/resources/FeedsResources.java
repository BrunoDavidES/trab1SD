package sd2223.trab1.server.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.clients.users.CheckUserClient;
import sd2223.trab1.server.UsersServer;

//ACABAR
//VAMOS TER DE CRIAR UM FEEDSSERVER TAMBÉM CERTO?
@Singleton
public class FeedsResources implements FeedsService {

	// VER MELHOR
	private long messageIdAssigner = 0;
	private final Map<String, Map<Long, Message>> feeds = new HashMap<String, Map<Long, Message>>();
	private final Map<String, List<String>> subscribed = new HashMap<String, List<String>>();
	// ACABAR INICIALIZAÇÃO PRA APONTAR PRA O DOMINIO CERTO
	//private final UsersServer remoteUsers = new UsersServer();
	//private final FeedsService remoteFeeds = null;
	private static Logger Log = Logger.getLogger(UsersResources.class.getName());

	public FeedsResources() {

	}

	// MAY LEAD TO ERROR
	// VER MELHOR
	@Override
	public long postMessage(String username, String pwd, Message msg) {
		if (username == null || pwd == null || msg == null) {
			Log.info("Null information was given");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		try {
			//CheckUserClient checker = new CheckUserClient();
		} catch (WebApplicationException e) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		Message message = new Message(this.messageIdAssigner, username, msg.getDomain(), msg.getText());
		this.messageIdAssigner++;
		if (!feeds.containsKey(username)) {
			Map<Long, Message> messages = new HashMap<Long, Message>();
			messages.put(message.getId(), message);
			feeds.put(username, messages);
		} else {
			feeds.get(username).put(message.getId(), message);
		}
		return msg.getId();

	}

	
	public void removeFromPersonalFeed(String username, long mid, String pwd) {
		if (username == null || pwd == null || mid == -1) {
			Log.info("Null information was given");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		try {
			//FAZER CHECK
			//remoteUsers.getUser(username, pwd);
		} catch (WebApplicationException e) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		feeds.get(username).remove(mid);
	}

	// SE DER ERRO VERIFICAR
	// VER MELHOR
	// PERGUNTAR
	// VER SE RETRY É SOLUÇÃO
	@Override
	public Message getMessage(String username, long mid) {
		if (username == null || mid == -1) {
			Log.info("Null information was given");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		Map<Long, Message> messages = feeds.get(username);
		if (messages == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		Message msg = messages.get(mid);
		if (msg == null) {
//			List<User> remote = subscribed.get(username);
//			int i = 0;
//			while (i < remote.size() && msg == null) {
//				User remoteUser = remote.get(i);
//				msg = remoteFeeds.getMessage(remoteUser.getName(), mid);
//				i++;
//			}
//			if (msg == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		return msg;
	}

	// PERGUNTAR SE PRECISAMOS DOS SUBSCRIBED
	@Override
	public List<Message> getMessages(String username, long time) {
		if (username == null || time == -1) {
			Log.info("Null information was given");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		List<Message> toList = new ArrayList<Message>();
		Collection<Message> messages = feeds.get(username).values();
		if (messages == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		Iterator<Message> it = messages.iterator();
		while (it.hasNext()) {
			Message itMessage = it.next();
			if (itMessage.getCreationTime() > time)
				toList.add(itMessage);
		}
//		List<User> remote = subscribed.get(username);
//		foreach(User u: remote) {
//			User remoteUser = remote.get(i);
//			msg = remoteFeeds.getMessage(remoteUser.getName(), mid);
//			i++;
//		}
//		if (msg == null)
		return toList;
	}

	//PERGUNTAR COMO BUSCAR USERSUB A OUTRO DOMÍNIO
	//FALTA TRATAR NOT_FOUND
	@Override
	public void subUser(String username, String userSub, String pwd) {
		if (username == null || userSub == null || pwd == null) {
			Log.info("Null information was given");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		try {
			//
			//emoteUsers.getUser(username, pwd);
		} catch (WebApplicationException e) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		//User toSubscribe = remoteFeeds.
		if(feeds.containsKey(userSub)) {
			if(!subscribed.containsKey(username)) {
				List<String> u = new ArrayList<String>();
				u.add(userSub);
				subscribed.put(username, u);
			}
			//else 
				
		}
	}

	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) {
		// TODO Auto-generated method stub

	}

	//PERGUNTAR SE 404 É NOT_FOUND NO FEED OU NO GERAL
	@Override
	public List<String> listSubs(String user) {
		return null;
	}

}
