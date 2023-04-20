package sd2223.trab1.clients.soap.feeds;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.soap.feeds.FeedsService;
import sd2223.trab1.api.soap.users.UsersService;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

public class SoapFeedsClient extends SoapClient implements Feeds {

	public SoapFeedsClient( URI serverURI ) {
		super( serverURI );
	}

	
	private FeedsService stub;
	synchronized private FeedsService stub() {
		if (stub == null) {
			QName QNAME = new QName(UsersService.NAMESPACE, UsersService.NAME);
			Service service = Service.create(super.toURL(super.uri + WSDL), QNAME);			
			this.stub = service.getPort(sd2223.trab1.api.soap.feeds.FeedsService.class);
			super.setTimeouts( (BindingProvider) stub);
		}
		return stub;
	}
	
	@Override
	public Result<Long> postMessage(String userANDdomain, String pwd, Message msg) {
		return super.reTry( () -> super.toJavaResult( () -> stub().postMessage(userANDdomain, pwd, msg) ) );
	}
	
	@Override
	public Result<Void> postSubMessage(String userANDdomain, Message msg) {
		//return super.reTry( () -> super.toJavaResult( () -> stub().postMessage(userANDdomain, pwd, msg) ) );
		return null;
	}
	
	@Override
	public Result<Void> addSubscriber(String userANDdomain, String sub) {
		//return super.reTry( () -> super.toJavaResult( () -> stub().postMessage(userANDdomain, pwd, msg) ) );
		return null;
	}
	

	@Override
	public Result<Void> removeFromPersonalFeed(String userANDdomain, long mid, String pwd) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Result<Void> removeFromSubscribedFeed(String userANDdomain, long mid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Result<Void> removeFromSubscribed(String userANDdomain, String sub) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Result<Void> removeFeed(String userANDdomain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Message> getMessage(String userANDdomain, long mid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<List<Message>> getMessages(String userANDdomain, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Void> subUser(String userANDdomain, String userSub, String pwd) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Result<Void> removeSubscriber(String userANDdomain, String sub) {
		//return super.reTry( () -> super.toJavaResult( () -> stub().postMessage(userANDdomain, pwd, msg) ) );
		return null;
	}

	@Override
	public Result<Void> unsubscribeUser(String userANDdomain, String userSub, String pwd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<List<String>> listSubs(String userANDdomain) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
