import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPSequentialServer implements ISHTTPSyncServer {

	public static final String SERVER_NAME = "SHTTPSequentialServer 0.1";

	private ServerSocket _listenSocket;
	private ServerConfig _config;
	private ServerCache  _serverCache;

	public SHTTPSequentialServer(ServerConfig config)
		throws IOException
	{
		_config = config;
		_listenSocket = new ServerSocket(_config.listenPort());
		_serverCache = new ServerCache(_config.cacheSize());
	}

	public boolean loadAvailable()
	{
		return true;	
	}

	public void run()
	{
		while (true) {
			Socket connectionSocket = null;
			
			try {
				connectionSocket = _listenSocket.accept();
			} catch (IOException ie) {
				System.err.println("Cannot accept connection: " + 
					ie.getMessage());
				continue;
			}
			Debug.DEBUG("Receiving request from " + connectionSocket);

			WebRequestHandler requestHandler;
			try {
				requestHandler = new WebRequestHandler(connectionSocket, 
					_config.documentRoot(), _serverCache, this, SERVER_NAME);
			} catch (IOException ie) {
				System.err.println("Cannot create request handler: " +
					ie.getMessage());
				continue;
			}
			requestHandler.processRequest();

			try {
				connectionSocket.close();
			} catch (IOException ie) {
				Debug.WARN("Error closing connection socket: " +
					ie.getMessage());
			}
		}
	}

	public static void usage()
	{
		System.out.println(
			"Usage: SHTTPSequentialServer -config <config_file_name>");
	}

	public static void main(String args[])
	{
		if (args.length != 2 || !args[0].equals("-config")) {
			usage();
			System.exit(1);
		}

		ServerConfig config = null;
		
		try {
			config = new ServerConfig(args[1]);
		} catch (ServerConfigException sce) {
			sce.printParserMessage();
			System.exit(1);
		} catch (IOException ie) {
			System.err.println("Error reading configuration file: " +
				ie.getMessage());
			System.exit(1);
		}

		config.print();

		SHTTPSequentialServer server = null;
		try {
			server = new SHTTPSequentialServer(config);
		} catch (IOException ie) {
			System.err.println("Cannot start server: " + ie.getMessage());
			System.exit(2);
		}
		server.run();
	}
}
