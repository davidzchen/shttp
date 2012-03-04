import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPPerRequestServer implements ISHTTPSyncServer {

	public static final String SERVER_NAME = "SHTTPPerRequestServer 0.1";

	private ServerSocket _listenSocket;
	private ServerConfig _config;
	private ServerCache  _serverCache;

	public SHTTPPerRequestServer(ServerConfig config)
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
			System.out.println("Receiving request from " + connectionSocket);

			SHTTPPerRequestThread thread = new SHTTPPerRequestThread(
				connectionSocket, _config.documentRoot(), _serverCache, 
				this);
			thread.start();
		}
	}

	public static void usage()
	{
		System.out.println(
			"Usage: SHTTPPerRequestServer -config <config_file_name>");
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

		SHTTPPerRequestServer server = null;
		try {
			server = new SHTTPPerRequestServer(config);
		} catch (IOException ie) {
			System.err.println("Cannot start server: " + ie.getMessage());
			System.exit(2);
		}
		server.run();
	}
}
