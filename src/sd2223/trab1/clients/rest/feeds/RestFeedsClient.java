package sd2223.trab1.clients.rest.feeds;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.GenericType;

public class RestFeedsClient extends RestClient implements Feeds {

	final WebTarget target;

	public RestFeedsClient(URI serverURI) {
		super(serverURI);
		target = client.target(serverURI).path(FeedsService.PATH);
	}


	@Override
	public Result<Long> postMessage(String userANDdomain, String pwd, Message msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Void> removeFromPersonalFeed(String userANDdomain, long mid, String pwd) {
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
