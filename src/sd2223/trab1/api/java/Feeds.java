package sd2223.trab1.api.java;

import java.util.List;

import sd2223.trab1.api.Message;

public interface Feeds {
	
	Result<Long> postMessage(String userANDdomain, String pwd, Message msg);
	
	Result<Void> postSubMessage(String sub, Message msg);
	
	Result<Void> removeFromPersonalFeed(String userANDdomain, long mid, String pwd);
	
	Result<Void> removeFeed(String userANDdomain);
	
	Result<Message> getMessage(String userANDdomain, long mid);
	
	Result<List<Message>> getMessages(String userANDdomain, long time);
	
	Result<Void> subUser(String userANDdomain, String userSub, String pwd);	
	
	Result<Void> addSubscriber(String userANDdomain, String sub);

	Result<Void> removeSubscriber(String userANDdomain, String sub);
	
	Result<Void> unsubscribeUser(String userANDdomain, String userSub, String pwd);
	
	Result<List<String>> listSubs(String userANDdomain);

}
