package sd2223.trab1.clients;

import java.net.URI;

import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.rest.users.RestUsersClient;
import sd2223.trab1.clients.soap.users.SoapUsersClient;
import sd2223.trab1.multicast.Discovery;
import sd2223.trab1.multicast.Domain;

public class UsersClientFactory {

	private static final String REST = "/rest";
	private static final String SOAP = "/soap";

	public static Users get(URI serverURI) {
		var uriString = serverURI.toString();

		if (uriString.endsWith(REST))
			return new RestUsersClient(serverURI);
		else if (uriString.endsWith(SOAP))
			return new SoapUsersClient(serverURI);
		else
			throw new RuntimeException("Unknown service type..." + uriString);
	}

	public static Users getUsersClient(String domain) {
		Discovery discovery = Discovery.getInstance();
		URI[] domainserviceURI = discovery.knownUrisOf(domain+":users", 1);
		var domainUsersClient = get(domainserviceURI[0]);
		return domainUsersClient;
//		return null;
	}

}
