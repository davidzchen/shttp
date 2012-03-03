import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.io.IOException;

public class Acceptor implements IAcceptHandler {

	private Dispatcher _dispatcher;
	private ServerSocketChannel _server;
	private ISocketReadWriteHandlerFactory _srwf;
	private ServerCache _serverCache;
	private String _documentRoot;


	public Acceptor(ServerSocketChannel server, Dispatcher dispatcher,
		ISocketReadWriteHandlerFactory srwf, ServerCache serverCache, 
		String documentRoot)
	{
		_dispatcher   = dispatcher;
		_server       = server;
		_srwf         = srwf;
		_serverCache  = serverCache;
		_documentRoot = documentRoot;
	}

	public void handleException()
	{
		System.out.println("handleException(): of Acceptor");
	}

	public void handleAccept(SelectionKey key)
		throws IOException
	{
		SocketChannel client = _server.accept();
		Debug.DEBUG("handleAccept: Accepted connection from " + client);

		client.configureBlocking(false);

		IReadWriteHandler rwH = _srwf.createHandler(_dispatcher, client,
			_serverCache, _documentRoot);
		int ops = rwH.getInitOps();

		SelectionKey clientKey = _dispatcher.registerNewSelection(client,
			rwH, ops);
	}
}
