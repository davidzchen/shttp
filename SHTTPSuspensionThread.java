import java.io.*;
import java.net.*;
import java.util.*;

class SHTTPSuspensionThread extends Thread {

	private List<Socket> _connectionSocketPool;
	private String _documentRoot;

	public SHTTPSuspensionThread(List<Socket> connectionSocketPool,
		String documentRoot)
	{
		_connectionSocketPool = connectionSocketPool;
		_documentRoot = documentRoot;
	}

	public void run()
	{
		while (true) {
			Socket connectionSocket = null;

			synchronized (_connectionSocketPool) {
				while (pool.isEmpty()) {
					try {
						System.out.println("Thread " + this + " 
							sees empty pool");
						pool.wait();
					} catch (InterruptedException e) {
						System.out.println("Waiting for pool interrupted.");
					}
				}

				connectionSocket = (Socket) pool.remove(0);
				System.out.println("Thread " + this + " process request " +
					connectionSocket);
			}

			WebRequestHandler requestHandler = new WebRequestHandler(
				connectionSocket, _documentRoot);
			requestHandler.processRequest();

			connectionSocket.close();
		}
	}
}
