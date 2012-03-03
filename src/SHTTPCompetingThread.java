import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPCompetingThread extends Thread {

	private SHTTPCompetingServer _server;
	private ServerSocket _listenSocket;
	private String _documentRoot;
	private ServerCache _serverCache;

	public SHTTPCompetingThread(ServerSocket listenSocket, String documentRoot,
		ServerCache serverCache, SHTTPCompetingServer server)
	{
		_server       = server;
		_listenSocket = listenSocket;
		_documentRoot = documentRoot;
		_serverCache  = serverCache;
	}

	public void run()
	{
		while (true) {
			Socket connectionSocket = null;

			synchronized (_listenSocket) {
				try {
					connectionSocket = _listenSocket.accept();
					System.out.println("Thread " + this + " accept request " +
						connectionSocket);
				} catch (IOException ie) {
					System.err.println("Cannot accept connection: " +
						ie.getMessage());
					continue;
				}
			}
			_server.incrLoad();

			WebRequestHandler requestHandler;
			
			try {
				requestHandler = new WebRequestHandler(connectionSocket, 
					_documentRoot, _serverCache, _server);
			} catch (IOException ie) {
				System.err.println("Cannot create request handler: " +
					ie.getMessage());
				_server.decrLoad();
				return;
			}
			requestHandler.processRequest();

			try {
				connectionSocket.close();
			} catch (IOException ie) {
				Debug.WARN("Error closing connection socket: " +
					ie.getMessage());
			}

			_server.decrLoad();
		}
	}
}
