package sd2223.trab1.server;

import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd2223.trab1.multicast.Discovery;
import sd2223.trab1.server.resources.FeedsResources;

public class FeedsServer {

	private static Logger Log = Logger.getLogger(FeedsServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "FeedsService";
	private static final String SERVER_URI_FMT = "http://%s.%s:%s/rest";
	private static final String MESSAGE = "%s:%s %s";

	public static void main(String[] args) {
		try {

			String domain = args[0];
			int id = Integer.parseInt(args[1]);

			ResourceConfig config = new ResourceConfig();
			FeedsResources instance = new FeedsResources(domain, id);
			config.register(instance.getClass());

			String serverURI = String.format(SERVER_URI_FMT, "feeds", domain, PORT);
			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

			Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));
			
			Discovery announcement = Discovery.getInstance();
			String announceMessage = String.format(MESSAGE, domain, "feeds", serverURI);
			announcement.announce(domain, announceMessage);

			// More code can be executed here...
		} catch (Exception e) {
			Log.severe(e.getMessage());
		}
	}

}
