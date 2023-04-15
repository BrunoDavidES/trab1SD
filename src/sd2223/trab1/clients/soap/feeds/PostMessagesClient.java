package sd2223.trab1.clients.soap.feeds;

import java.net.URI;

import sd2223.trab1.api.User;

public class PostMessagesClient {

	public static void main(String[] args) {
		if( args.length != 5) {
			System.err.println("usage: serverUri name pwd domain displayName");
			System.exit(0);
		}
		
		var serverURI = args[0];
		var name = args[1];
		var pwd = args[2];
		var domain = args[3];
		var displayName = args[4];
		
		var feeds = new SoapFeedsClient( URI.create( serverURI ));
		
//		var res = feeds.postMessage( new M( name, pwd, domain, displayName) );
//		System.out.println( res );
	}

}
