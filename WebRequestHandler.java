import java.io.*;
import java.net.*;
import java.util.*;

class WebRequestHandler {

	private Socket _connectionSocket;
	private String _documentRoot;
	private BufferedReader _inFromClient;
	private DataOutputStream _outToClient;

	public WebRequestHandler(Socket connectionSocket, String documentRoot)
	{
		_documentRoot = documentRoot;
		_connectionSocket = connectionSocket;

		_inFromClient = new BufferedReader(new InputStreamReader(
			_connectionSocket.getInputStream()));

		_outToClient = new DataOutputStream(
			_connectionSocket.getOutputStream());
	}


	

}
