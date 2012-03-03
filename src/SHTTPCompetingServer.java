import java.io.*;
import java.util.*;
import java.net.*;

public class SHTTPCompetingServer implements ISHTTPSyncServer {

	private ServerSocket _listenSocket;
	private ServerConfig _config;
	private ServerCache _serverCache;
	private SHTTPCompetingThread[] _threads;
	private Integer _busyThreads;

	public SHTTPCompetingServer(ServerConfig config)
		throws IOException
	{
		_busyThreads = 0;
		_config = config;
		_listenSocket = new ServerSocket(_config.listenPort());
		_serverCache = new ServerCache(_config.cacheSize()); 

		_threads = new SHTTPCompetingThread[_config.threadPoolSize()];

		for (int i = 0; i < _threads.length; i++) {
			_threads[i] = new SHTTPCompetingThread(_listenSocket,
				_config.documentRoot(), _serverCache, this);
			_threads[i].start();
		}
	}

	public boolean loadAvailable()
	{
		boolean available = false;

		synchronized (_busyThreads) {
			available = (_busyThreads == _config.threadPoolSize());
		}
		return available;
	}

	public void incrLoad()
	{
		synchronized (_busyThreads) {
			_busyThreads++;
		}
	}

	public void decrLoad()
	{
		synchronized (_busyThreads) {
			_busyThreads--;
		}
	}

	public void run()
	{
		try {
			for (int i = 0; i < _threads.length; i++) {
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
}

