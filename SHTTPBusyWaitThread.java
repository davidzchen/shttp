import java.io.*;
import java.util.*;
import java.net.*;

class SHTTPBusyWaitThread extends Thread {

	private List<Socket> _connectionPool;
	private String _documentRoot;

	public SHTTPBusyWaitThread(List<Socket> connectionPool, String documentRoot)
	{
		_connectionPool = connectionPool;
		_documentRoot = documentRoot;
	}

	public void run() 
	{
		while (true) {
			Socket connectionSocket = null;

			while (connectionSocket == null) {
				synchronized (_connectionPool) {
					if (!_connectionPool.isEmpty()) {
						connectionSocket = (Socket) pool.remove(0);
						System.out.println("Thread " + this + " process " +
							"request " + connectionSocket);
					}
				}
			}

			WebRequestHandler requestHandler = new WebRequestHandler(
				connectionSocket, _documentRoot);
			requestHandler.processRequest();

			connectionSocket.close();
		}
	}
}
