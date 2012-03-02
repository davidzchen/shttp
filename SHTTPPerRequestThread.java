import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPPerRequestThread extends Thread {

	private SHTTPPerRequestServer _server;
	private Socket _connectionSocket;
	private String _documentRoot;
	private ServerCache _serverCache;

	public SHTTPPerRequestThread(Socket connectionSocket,
		String documentRoot, ServerCache serverCache,
		SHTTPPerRequestServer server)
	{
		_server = server;
		_connectionSocket = connectionSocket;
		_documentRoot = documentRoot;
		_serverCache = serverCache;
	}

	public void run()
	{
		WebRequestHandler requestHandler;
		
		try {
			requestHandler = new WebRequestHandler(_connectionSocket, 
				_documentRoot, _serverCache, _server);
		} catch (IOException ie) {
			System.err.println("Cannot create request handler: " +
				ie.getMessage());
			return;
		}
		requestHandler.processRequest();

		try {
			_connectionSocket.close();
		} catch (IOException ie) {
			Debug.WARN("Error closing connection socket: " +
				ie.getMessage());
		}
	}

}
