import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.io.IOException;

public class SHTTPReadWriteHandler implements IReadWriteHandler {

	private Dispatcher _dispatcher;
	private SocketChannel _client;

	private boolean _requestComplete;
	private boolean _responseReady;
	private boolean _responseSent;
	private boolean _channelClosed;

	private int _requestTermCount;

	private ServerCache _serverCache;
	private String _documentRoot;

	private SHTTPRequest _request;
	private SHTTPResponse _response;
	private ByteBuffer _inBuffer;
	private ByteBuffer _outBuffer;

	private StringBuffer _requestBuffer;

	public SHTTPReadWriteHandler(Dispatcher dispatcher, SocketChannel client,
		ServerCache serverCache, String documentRoot)
	{
		_dispatcher   = dispatcher;
		_client       = client;
		_serverCache  = serverCache;
		_documentRoot = documentRoot;

		_requestComplete = false;
		_responseReady    = false;
		_responseSent    = false;
		_channelClosed   = false;

		_inBuffer  = ByteBuffer.allocate(4096);
		_outBuffer = ByteBuffer.allocate(4096);

		_requestBuffer = new StringBuffer(4096);
		_requestTermCount = 0;
	}
	
	public int getInitOps()
	{
		return SelectionKey.OP_READ;	
	}

	public void handleException()
	{
	
	}

	private void _processInBuffer()
		throws IOException
	{
		Debug.DEBUG("  > processInBuffer()");
		int readBytes = _client.read(_inBuffer);
		Debug.DEBUG("  > handleRead: Read data from connection " + _client +
			" for " + readBytes +
			" byte(s); to buffer " + _inBuffer);

		if (readBytes == -1) {
			_requestComplete = true;
			Debug.DEBUG("  > handleRead: readBytes == -1");
		} else {
			_inBuffer.flip();
			while (!_requestComplete && _inBuffer.hasRemaining() &&
				   _requestBuffer.length() < _requestBuffer.capacity()) {
				char ch = (char) _inBuffer.get();
				_requestBuffer.append(ch);
				if (ch == '\r') {
					if (_requestTermCount == 0 || _requestTermCount == 2)
						_requestTermCount++;
				} else if (ch == '\n') {
					if (_requestTermCount == 1) {
						_requestTermCount++;
					} else {
						_requestComplete = true;
						_requestTermCount = 0;
					}
				} else {
					_requestTermCount = 0;
				}
			}
		}

		_inBuffer.clear();

		if (_requestComplete) {
			_processRequest();
		}
	}

	public void _processRequest()
		throws IOException
	{
		try {
			_request = new SHTTPRequest(_requestBuffer);
		} catch (SHTTPRequestException se) {
			_generateErrorReply(se.getErrorCode(), se.getMessage());
			_responseReady = true;
			return;
		}

		try {
			_mapURLToFile();
		} catch (SHTTPRequestException se) {
			_generateErrorReply(se.getErrorCode(), se.getMessage());
			_responseReady = true;
			return;
		}

		

	}

	private void _mapURLToFile()
	{

	}

	public void _generateErrorReply(int errorCode, String message)
	{
		_response = new SHTTPResponse();
		_response.setStatus(errorCode, message);
		_response.setContent(
			"<html>" +
			"<h1>" + errorCode + ": " + message + "</h1>" +
			"</html>");
	}

	public void handleRead(SelectionKey key)
		throws IOException
	{
		Debug.DEBUG("--> START handleRead()");

		if (_requestComplete)
			return;

		_processInBuffer();
		_updateDispatcher();		

		Debug.DEBUG("--> DONE  handleRead()");
	}

	public void handleWrite(SelectionKey key)
		throws IOException
	{
		Debug.DEBUG("--> START handleWrite()");

		// Write reply

		Debug.DEBUG("--> DONE  handleWrite()");
	}

	private void _updateDispatcher()
		throws IOException
	{
		Debug.DEBUG("--> START Update dispatcher.");

		if (_channelClosed)
			return;

		SelectionKey selectionKey = _dispatcher.keyFor(_client);

		if (_responseSent) {
			Debug.DEBUG("*** Response sent; Connection closed");
			dispatcher.deregisterSelection(selectionKey);
			_client.close();
			_channelClosed = true;
			return;
		}

		int nextState = 0;
		if (_requestComplete) {
			nextState = nextState & ~SelectionKey.OP_READ;
			Debug.DEBUG("New state: -Read (request parsing complete)");
		} else {
			nextState = nextState | SelectionKey.OP_READ;
			Debug.DEBUG("New state: +Read (continue to read request)");
		}

		if (_responseReady) {
			nextState = SelectionKey.OP_WRITE;
			Debug.DEBUG("New state: +Write (Response ready but not sent)");
		}

		dispatcher.updateInterests(selectionKey, nextState);
		Debug.DEBUG("--> DONE  Update dispatcher.");
	}
}
