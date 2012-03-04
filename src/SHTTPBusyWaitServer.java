import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPBusyWaitServer implements ISHTTPSyncServer {

	public static final String SERVER_NAME = "SHTTPBusyWaitServer 0.1";

	private static final int SERVER_LOAD_THRESHOLD = 20;

	private ServerSocket _listenSocket;
	private ServerConfig _config;
	private ServerCache _serverCache;
	private List<Socket> _connectionSocketPool;
	private SHTTPBusyWaitThread[] _threads;

	public SHTTPBusyWaitServer(ServerConfig config)
		throws IOException
	{
		_config = config;
		_listenSocket = new ServerSocket(_config.listenPort());
		_serverCache = new ServerCache(_config.cacheSize());

		_connectionSocketPool = new Vector<Socket>();
		_threads = new SHTTPBusyWaitThread[_config.threadPoolSize()];

		for (int i = 0; i < _threads.length; i++) {
			_threads[i] = new SHTTPBusyWaitThread(_connectionSocketPool,
				_config.documentRoot(), _serverCache, this);
			_threads[i].start();
		}
	}

	public boolean loadAvailable()
	{
		int size;

		synchronized (_connectionSocketPool) {
			size = _connectionSocketPool.size();
		}

		return (size < SERVER_LOAD_THRESHOLD);
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
			System.out.println("Main thread accept connection " +
				connectionSocket);

			synchronized (_connectionSocketPool) {
				_connectionSocketPool.add(connectionSocket);
			}
		}
	}

	public static void usage()
	{
		System.out.println(
			"Usage: SHTTPBusyWaitServer -config <config_file_name>");
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

		SHTTPBusyWaitServer server = null;
		try {
			server = new SHTTPBusyWaitServer(config);
		} catch (IOException ie) {
			System.err.println("Cannot start server: " + ie.getMessage());
			System.exit(2);
		}
		server.run();
	}
}
