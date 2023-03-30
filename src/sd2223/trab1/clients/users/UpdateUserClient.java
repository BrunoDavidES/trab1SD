package sd2223.trab1.clients.users;

import java.io.IOException;

import org.glassfish.jersey.client.ClientConfig;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.UsersService;

public class UpdateUserClient {
	public static void main(String[] args) throws IOException {

		if (args.length != 6) {
			System.err.println("Use: java aula2.clients.UpdateUserClient url name oldpwd domain displayName pwd");
			return;
		}

		String serverUrl = args[0];
		String name = args[1];
		String oldpwd = args[2];
		String domain = args[3];
		String displayName = args[4];
		String pwd = args[5];

		var user = new User(name, pwd, domain, displayName);

		System.out.println("Sending request to server.");

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		WebTarget target = client.target(serverUrl).path(UsersService.PATH);

		Response response = target.path(name).queryParam(UsersService.PWD, oldpwd).request()
				.accept(MediaType.APPLICATION_JSON).put(Entity.entity(user, MediaType.APPLICATION_JSON));

		if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity())
			System.out.println("Success, updated user with id: " + response.readEntity(String.class));
		else
			System.out.println("Error, HTTP error status: " + response.getStatus());
	}

}
