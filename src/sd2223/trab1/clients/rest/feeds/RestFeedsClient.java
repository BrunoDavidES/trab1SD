package sd2223.trab1.clients.rest.feeds;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Singleton;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.clients.rest.RestClient;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.GenericType;

@Singleton
public class RestFeedsClient extends RestClient implements Feeds {

	final WebTarget target;

	public RestFeedsClient(URI serverURI) {
		super(serverURI);
		target = client.target(serverURI).path(FeedsService.PATH);
	}

	private Result<Long> clt_postMessage(String userANDdomain, String pwd, Message msg) {
		Response r = target.path(userANDdomain).queryParam(UsersService.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON).post(Entity.entity(msg, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, Long.class);

	}

	private Result<Void> clt_postSubMessage(String userANDdomain, Message msg) {
		Response r = target.path(FeedsService.PROPAGATE_POST).path(userANDdomain).request()
				.accept(MediaType.APPLICATION_JSON).post(Entity.entity(msg, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, Void.class);

	}

	private Result<Void> clt_removeFromPersonalFeed(String userANDdomain, long mid, String pwd) {
		Response r = target.path(userANDdomain).path(Long.toString(mid)).queryParam(UsersService.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON).delete();
		return super.toJavaResult(r, Void.class);

	}

	private Result<Void> clt_removeFromSubscribedFeed(String userANDdomain, long mid) {
		Response r = target.path(FeedsService.PROPAGATE_DELETE).path(userANDdomain).path(Long.toString(mid)).request()
				.delete();
		return super.toJavaResult(r, Void.class);

	}

	private Result<Void> clt_removeFeed(String userANDdomain) {
		Response r = target.path(userANDdomain).request().accept(MediaType.APPLICATION_JSON).delete();
		return super.toJavaResult(r, Void.class);

	}

	private Result<Void> clt_removeFromSubscribed(String userANDdomain, String sub) {
		Response r = target.path(FeedsService.PROPAGATE_DELETE_SUB).path(userANDdomain).path(sub).request().delete();
		return super.toJavaResult(r, Void.class);

	}

	private Result<Message> clt_getMessage(String userANDdomain, long mid) {
		Response r = target.path(userANDdomain).path(Long.toString(mid)).request().accept(MediaType.APPLICATION_JSON)
				.get();
		return super.toJavaResult(r, Message.class);
	}

	private Result<List<Message>> clt_getMessages(String userANDdomain, long time) {
		Response r = target.path(userANDdomain).queryParam(FeedsService.TIME, time).request()
				.accept(MediaType.APPLICATION_JSON).get();
		return super.toJavaResult(r, new GenericType<List<Message>>() {
		});
	}

	private Result<Void> clt_subUser(String userANDdomain, String userSub, String pwd) {
		Response r = target.path(userANDdomain).path(userSub).queryParam(UsersService.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON).post(Entity.entity(userSub, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, Void.class);
	}

	private Result<Void> clt_addSubscriber(String userANDdomain, String sub) {
		Response r = target.path(FeedsService.ADD_SUBSCRIBER).path(userANDdomain).path(sub).request()
				.accept(MediaType.APPLICATION_JSON).post(Entity.entity(sub, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, Void.class);
	}

	private Result<Void> clt_unsubscribeUser(String userANDdomain, String userSub, String pwd) {
		Response r = target.path(userANDdomain).path(userSub).queryParam(UsersService.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON).delete();
		return super.toJavaResult(r, Void.class);
	}

	private Result<Void> clt_removeSubscriber(String userANDdomain, String sub) {
		Response r = target.path(FeedsService.DEL_SUBSCRIBER).path(userANDdomain).path(sub).request()
				.accept(MediaType.APPLICATION_JSON).delete();
		return super.toJavaResult(r, Void.class);
	}

	private Result<List<String>> clt_listSubs(String userANDdomain) {
		Response r = target.path(userANDdomain).request().accept(MediaType.APPLICATION_JSON).get();
		return super.toJavaResult(r, new GenericType<List<String>>() {
		});
	}

	@Override
	public Result<Long> postMessage(String userANDdomain, String pwd, Message msg) {
		return super.reTry(() -> clt_postMessage(userANDdomain, pwd, msg));
	}

	@Override
	public Result<Void> postSubMessage(String userANDdomain, Message msg) {
		return super.reTry(() -> clt_postSubMessage(userANDdomain, msg));
	}

	@Override
	public Result<Void> removeFromPersonalFeed(String userANDdomain, long mid, String pwd) {
		return super.reTry(() -> clt_removeFromPersonalFeed(userANDdomain, mid, pwd));
	}

	@Override
	public Result<Void> removeFromSubscribedFeed(String userANDdomain, long mid) {
		return super.reTry(() -> clt_removeFromSubscribedFeed(userANDdomain, mid));
	}

	@Override
	public Result<Void> removeFeed(String userANDdomain) {
		return super.reTry(() -> clt_removeFeed(userANDdomain));
	}

	@Override
	public Result<Void> removeFromSubscribed(String userANDdomain, String sub) {
		return super.reTry(() -> clt_removeFromSubscribed(userANDdomain, sub));
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
	public Result<Void> addSubscriber(String userANDdomain, String sub) {
		return super.reTry(() -> clt_addSubscriber(userANDdomain, sub));
	}

	@Override
	public Result<Void> removeSubscriber(String userANDdomain, String sub) {
		return super.reTry(() -> clt_removeSubscriber(userANDdomain, sub));
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