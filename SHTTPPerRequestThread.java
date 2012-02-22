import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPPerRequestThread extends Thread {

	private Socket _connectionSocket;
	private String _documentRoot;

	public SHTTPPerRequestThread(Socket connectionSocket,
		String documentRoot)
	{
		_connectionSocket = connectionSocket;
		_documentRoot = documentRoot;
	}

	public void run()
	{
		WebRequestHandler requestHandler = new WebRequestHandler(
			_connectionSocket, _documentRoot);
		requestHandler.processRequest();

		_connectionSocket.close();
	}

}
