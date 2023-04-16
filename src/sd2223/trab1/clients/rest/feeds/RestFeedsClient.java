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

	private Result<Long> clt_postMessage(String userANDdomain, String pwd, Message msg) {

	}

	private Result<Void> clt_removeFromPersonalFeed(String userANDdomain, long mid, String pwd) {

	}

	private Result<Message> clt_getMessage(String userANDdomain, long mid) {

	}

	private Result<List<Message>> clt_getMessages(String userANDdomain, long time) {

	}

	private Result<Void> clt_subUser(String userANDdomain, String userSub, String pwd) {

	}

	private Result<Void> clt_unsubscribeUser(String userANDdomain, String userSub, String pwd) {

	}

	private Result<List<String>> clt_listSubs(String userANDdomain) {

	}

	@Override
	public Result<Long> postMessage(String userANDdomain, String pwd, Message msg) {
		return super.reTry(() -> clt_postMessage(userANDdomain, pwd, msg));
	}

	@Override
	public Result<Void> removeFromPersonalFeed(String userANDdomain, long mid, String pwd) {
		return super.reTry(() -> clt_removeFromPersonalFeed(userANDdomain, mid, pwd));
	}

	@Override
	public Result<Message> getMessage(String userANDdomain, long mid) {
		return super.reTry(() -> clt_getMessage(userANDdomain, mid));
	}

	@Override
	public Result<List<Message>> getMessages(String userANDdomain, long time) {
		return super.reTry(() -> clt_getMessages(userANDdomain, time));
	}

	@Override
	public Result<Void> subUser(String userANDdomain, String userSub, String pwd) {
		return super.reTry(() -> clt_subUser(userANDdomain, userSub, pwd));
	}

	@Override
	public Result<Void> unsubscribeUser(String userANDdomain, String userSub, String pwd) {
		return super.reTry(() -> clt_unsubscribeUser(userANDdomain, userSub, pwd));
	}

	@Override
	public Result<List<String>> listSubs(String userANDdomain) {
		return super.reTry(() -> clt_listSubs(userANDdomain));
	}
}
