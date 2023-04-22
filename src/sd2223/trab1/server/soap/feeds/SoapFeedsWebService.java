package sd2223.trab1.server.soap.feeds;

import java.util.List;
import java.util.logging.Logger;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.soap.feeds.FeedsException;
import sd2223.trab1.api.soap.feeds.FeedsService;
import sd2223.trab1.server.java.JavaFeeds;
import sd2223.trab1.server.soap.SoapWebService;
import jakarta.jws.WebService;

@WebService(serviceName = FeedsService.NAME, targetNamespace = FeedsService.NAMESPACE, endpointInterface = FeedsService.INTERFACE)
public class SoapFeedsWebService extends SoapWebService<FeedsException> implements FeedsService {

	static Logger Log = Logger.getLogger(SoapFeedsWebService.class.getName());

	final Feeds impl;

	public SoapFeedsWebService() {
		super((result) -> new FeedsException(result.error().toString()));
		this.impl = new JavaFeeds();
	}

	@Override
	public long postMessage(String user, String pwd, Message msg) throws FeedsException {
		return super.fromJavaResult(impl.postMessage(user, pwd, msg));
	}

	@Override
	public void postSubMessage(String user, Message msg) throws FeedsException {
		super.fromJavaResult(impl.postSubMessage(user, msg));
	}

	@Override
	public void removeFromPersonalFeed(String user, long mid, String pwd) throws FeedsException {
		super.fromJavaResult(impl.removeFromPersonalFeed(user, mid, pwd));
	}

	@Override
	public void removeFeed(String user) throws FeedsException {
		super.fromJavaResult(impl.removeFeed(user));
	}

	@Override
	public void removeFromSubscribedFeed(String user, long mid) throws FeedsException {
		super.fromJavaResult(impl.removeFromSubscribedFeed(user, mid));
	}

	@Override
	public void removeFromSubscribed(String user, String sub) throws FeedsException {
		super.fromJavaResult(impl.removeFromSubscribed(user, sub));
	}

	@Override
	public Message getMessage(String user, long mid) throws FeedsException {
		return super.fromJavaResult(impl.getMessage(user, mid));
	}

	@Override
	public List<Message> getMessages(String user, long time) throws FeedsException {
		return super.fromJavaResult(impl.getMessages(user, time));
	}

	@Override
	public void subUser(String user, String userSub, String pwd) throws FeedsException {
		super.fromJavaResult(impl.subUser(user, userSub, pwd));
	}

	@Override
	public void addSubscriber(String user, String userSub) throws FeedsException {
		super.fromJavaResult(impl.addSubscriber(user, userSub));
	}

	@Override
	public void unsubscribeUser(String user, String userSub, String pwd) throws FeedsException {
		super.fromJavaResult(impl.unsubscribeUser(user, userSub, pwd));
	}

	@Override
	public void removeSubscriber(String user, String userSub) throws FeedsException {
		super.fromJavaResult(impl.removeSubscriber(user, userSub));
	}

	@Override
	public List<String> listSubs(String user) throws FeedsException {
		return super.fromJavaResult(impl.listSubs(user));
	}

}
