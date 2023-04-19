package sd2223.trab1.server.rest.users;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd2223.trab1.multicast.Discovery;
import sd2223.trab1.multicast.Domain;
import sd2223.trab1.server.java.JavaUsers;

public class RestUsersServer {

	private static Logger Log = Logger.getLogger(RestUsersServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "UsersService";
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";
	private static final String MESSAGE = "%s:%s %s";

	public static void main(String[] args) {
		try {

			String domain = args[0];
			
			Domain.setDomain(domain);

			ResourceConfig config = new ResourceConfig();
			config.register(new RestUsersResource());
			Domain.setDomain(domain);

			String ip = InetAddress.getLocalHost().getHostAddress();

			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

			Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

			System.out.println("REACHED INSTANCE");
//			String toAnnounce = String.format(MESSAGE, domain, "users", serverURI);
//			Log.info(toAnnounce);
			Discovery announcement = Discovery.getInstance();
			System.out.println("REACHED ANNOUNCEMENT");
			announcement.announce(domain+":users", serverURI);
			System.out.println("ANNOUNCED");

			// More code can be executed here...
		} catch (Exception e) {
			Log.severe(e.getMessage());
		}
	}
}
