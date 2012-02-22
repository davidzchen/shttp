import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPCompetingThread extends Thread {

	private ServerSocket _listenSocket;
	private String _documentRoot;

	public SHTTPCompetingThread(ServerSocket listenSocket, String documentRoot)
	{
		_listenSocket = listenSocket;
		_documentRoot = documentRoot;
	}

	public void run()
	{
		while (true) {
			Socket connectionSocket = null;

			synchronized (_listenSocket) {
				try {
					connectionSocket = welcomeSocket.accept();
					System.out.println("Thread " + this + " accept request " +
						connectionSocket);
				} catch (IOException ie) {
					System.err.println("Cannot accept connection: " +
						ie.getMessage());
					continue;
				}
			}

			WebRequestHandler requestHandler = new WebRequestHandler(
				connectionSocket, _documentRoot);
			requestHandler.processRequest();

			connectionSocket.close();
		}
	}

}
