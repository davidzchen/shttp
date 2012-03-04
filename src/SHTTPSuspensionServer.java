import java.io.*;
import java.util.*;
import java.net.*;

public class SHTTPSuspensionServer implements ISHTTPSyncServer {

	public static final String SERVER_NAME = "SHTTPSuspensionServer 0.1";

	private static final int SERVER_LOAD_THRESHOLD = 20;

	private ServerSocket _listenSocket;
	private ServerConfig _config;
	private ServerCache _serverCache;
	private List<Socket> _connectionSocketPool;
	private SHTTPSuspensionThread[] _threads;

	public SHTTPSuspensionServer(ServerConfig config)
		throws IOException
	{
		_config = config;
		_listenSocket = new ServerSocket(_config.listenPort());
		_serverCache = new ServerCache(_config.cacheSize());

		_connectionSocketPool = new Vector<Socket>();
		_threads = new SHTTPSuspensionThread[_config.threadPoolSize()];

		for (int i = 0; i < _threads.length; i++) {
			_threads[i] = new SHTTPSuspensionThread(_connectionSocketPool,
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
				System.err.println("Cannot receive connection: " +
					ie.getMessage());
				continue;
			}
			System.out.println("Main thread accept connection " +
				connectionSocket);

			synchronized (_connectionSocketPool) {
				_connectionSocketPool.add(connectionSocket);
				_connectionSocketPool.notifyAll();
			}
		}
	}

	public static void usage()
	{
		System.out.println(
			"Usage: SHTTPSuspensionServer -config <config_file_name>");
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

		SHTTPSuspensionServer server = null;
		try {
			server = new SHTTPSuspensionServer(config);
		} catch (IOException ie) {
			System.err.println("Cannot start server: " + ie.getMessage());
			System.exit(2);
		}
		server.run();
	}
}
