import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class SHTTPAsyncServer {

	public static ServerSocketChannel openServerChannel(int port)
	{
		ServerSocketChannel serverChannel = null;

		serverChannel = SocketServerChannel.open();

		/* Extract server socket of the server channel and bind the port. */
		ServerSocket serverSocket = serverChannel.socket();
		InetSocketAddress address = new InetSocketAddress(port);
		serverSocket.bind(address);

		/* Configure to be non-blocking. */
		serverChannel.configureBlocking(false);

		Debug.DEBUG("Server listening for connections on port " + port);

		return serverChannel;
	}

	public static void usage()
	{
		System.out.println(
			"Usage: SHTTPAsyncServer -config <config_file_name>");
	}

	public static void main(String[] args)
	{
		if (args.length != 2 || !args[0].equals("-config")) {
			usage();
			System.exit(1);
		}

		/* Read server config file. */
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

		ServerCache serverCache = new ServerCache(config.cacheSize());

		/* Get dispatcher/selector */
		Dispatcher dispatcher = new Dispatcher(serverCache, config);

		/* Open server socket channel. */
		int port = config.listenPort();
		ServerSocketChannel socketChannel = openServerChannel(port);

		/* Create server acceptor for SHTTP ReadWrite Handler. */
		ISocketReadWriteHandlerFactory shttpFactory =
			new SHTTPReadWriteHandlerFactory();
		Acceptor acceptor = new Acceptor(socketChannel, dispatcher, 
			shttpFactory, serverCache, config.documentRoot());

		Thread dispatcherThread;

		/* Register server channel to a selector. */
		try {
			SelectionKey dispatcher.registerNewSelection(
				socketChannel, acceptor, SelectionKey.OP_ACCEPT);

			dispatcherThread = new Thread(dispatcher);
			dispatcherThread.start();
		} catch (IOException ie) {
			System.out.println("Cannot register and start server: " +
				ie.getMessage());
			ie.printStackTrace();
			System.exit(1);
		}

		// XXX Join the dispatcher thread??
	}
}
