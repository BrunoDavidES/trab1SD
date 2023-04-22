package sd2223.trab1.clients;

import java.net.URI;

import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.rest.feeds.RestFeedsClient;
import sd2223.trab1.clients.soap.feeds.SoapFeedsClient;
import sd2223.trab1.multicast.Discovery;
import sd2223.trab1.multicast.Domain;

public class FeedsClientFactory {

	private static final String REST = "/rest";
	private static final String SOAP = "/soap";

	public static Feeds get(URI serverURI) {
		var uriString = serverURI.toString();

		if (uriString.endsWith(REST))
			return new RestFeedsClient(serverURI);
		else if (uriString.endsWith(SOAP))
			return new SoapFeedsClient(serverURI);
		else
			throw new RuntimeException("Unknown service type..." + uriString);
	}

	public static Feeds getFeedsClient(String domain) {
		Discovery discovery = Discovery.getInstance();
		URI[] domainserviceURI = discovery.knownUrisOf(domain+":feeds", 1);
		return get(domainserviceURI[0]);
	}
	
}
