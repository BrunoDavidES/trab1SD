package sd2223.trab1.clients;

import java.net.URI;

import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.clients.rest.feeds.RestFeedsClient;
import sd2223.trab1.clients.soap.feeds.SoapFeedsClient;

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
}
