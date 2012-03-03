import java.io.*;
import java.net.*;
import java.util.*;

class SHTTPSuspensionThread extends Thread {

	private SHTTPSuspensionServer _server;
	private List<Socket> _connectionSocketPool;
	private String _documentRoot;
	private ServerCache _serverCache;

	public SHTTPSuspensionThread(List<Socket> connectionSocketPool,
		String documentRoot, ServerCache serverCache, 
		SHTTPSuspensionServer server)
	{
		_server = server;
		_connectionSocketPool = connectionSocketPool;
		_documentRoot = documentRoot;
		_serverCache = serverCache;
	}

	public void run()
	{
		while (true) {
			Socket connectionSocket = null;

			synchronized (_connectionSocketPool) {
				while (_connectionSocketPool.isEmpty()) {
					try {
						System.out.println("Thread " + this + 
							"sees empty pool");
						_connectionSocketPool.wait();
					} catch (InterruptedException e) {
						System.out.println("Waiting for pool interrupted.");
					}
				}

				connectionSocket = (Socket) _connectionSocketPool.remove(0);
				System.out.println("Thread " + this + " process request " +
					connectionSocket);
			}

			WebRequestHandler requestHandler;
			
			try {
				requestHandler = new WebRequestHandler(connectionSocket,
					_documentRoot, _serverCache, _server);
			} catch (IOException ie) {
				System.err.println("Cannot create request handler: " +
					ie.getMessage());
				return;
			}
			requestHandler.processRequest();

			try {
				connectionSocket.close();
			} catch (IOException ie) {
				System.err.println("Error closing connection socket: " +
					ie.getMessage());
			}
		}
	}
}
