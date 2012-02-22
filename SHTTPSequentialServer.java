import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPSequentialServer {

	private ServerSocket _listenSocket;
	private ServerConfig _config;

	public SHTTPSequentialServer(ServerConfig config)
		throws IOException
	{
		_config = config;
		_listenSocket = new ServerSocket(_config.listenPort());
		_config.print();
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

			WebRequestHandler requestHandler = new WebRequestHandler(
				connectionSocket, _config.documentRoot());
			requestHandler.processRequest();

			connectionSocket.close();
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
