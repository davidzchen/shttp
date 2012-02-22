import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPCompetingServer {

	private ServerSocket _listenSocket;
	private ServerConfig _config;
	private SHTTPCompetingThread[] _threads;

	public SHTTPCompetingServer(ServerConfig config)
	{
		_config = config;
		_listenSocket = new ServerSocket(_config.listenPort());
		_config.print();

		_threads = new SHTTPCompetingThread[_config.threadPoolSize()];

		for (int i = 0; i < _threads.length; i++) {
			_threads[i] = new SHTTPCompetingThread(listenSocket,
				_config.documentRoot());
			_threads[i].start();
		}
	}

	public void run()
	{
		try {
			for (int i = 0; i < threads.length; i++) {
				_threads[i].join();
			}
			System.out.println("All threads finished. Exit.");
		} catch (Exception e) {
			System.err.println("Join errors.");
		}

	}

	public static void usage()
	{
		System.out.println(
			"Usage: SHTTPCompetingServer -config <config_file_name>");
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

		SHTTPCompetingServer server = null;
		try {
			server = new SHTTPCompetingServer(config);
		} catch (IOException ie) {
			System.err.println("Cannot start server: " + ie.getMessage());
			System.exit(2);
		}
		server.run();
}
