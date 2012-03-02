import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPBusyWaitThread extends Thread {

	private SHTTPBusyWaitServer _server;
	private List<Socket> _connectionPool;
	private String _documentRoot;
	private ServerCache _serverCache;

	public SHTTPBusyWaitThread(List<Socket> connectionPool, String documentRoot,
		ServerCache serverCache, SHTTPBusyWaitServer server)
	{
		_server         = server;
		_connectionPool = connectionPool;
		_documentRoot   = documentRoot;
		_serverCache    = serverCache;
	}

	public void run() 
	{
		while (true) {
			Socket connectionSocket = null;

			while (connectionSocket == null) {
				synchronized (_connectionPool) {
					if (!_connectionPool.isEmpty()) {
						connectionSocket = (Socket) _connectionPool.remove(0);
						System.out.println("Thread " + this + " process " +
							"request " + connectionSocket);
					}
				}
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
				Debug.WARN("Error closing connection socket: " +
					ie.getMessage());
			}
		}
	}
}
