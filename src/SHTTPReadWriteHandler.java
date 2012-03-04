import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.io.*;

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

	/* Response. */
	private boolean _loadBalancer;
	private boolean _notModified;
	private byte[] _fileContents;
	private File _fileInfo;
	private String _contentType;

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
		/* What exception? */	
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
		/* Parse request object from request buffer. */
		try {
			_request = new SHTTPRequest(_requestBuffer);
		} catch (SHTTPRequestException se) {
			_generateErrorReply(se.getMessage(), se.getErrorCode());
			_responseReady = true;
			return;
		}

		/* Map request to file */
		try {
			_mapURLToFile();
		} catch (SHTTPRequestException se) {
			_generateErrorReply(se.getMessage(), se.getErrorCode());
			_responseReady = true;
			return;
		} catch (IOException ie) {
			System.err.println("Cannot read file " +
				ie.getMessage());
			_generateErrorReply("Internal Server Error", 
				Status.INTERNAL_SERVER_ERROR);
			_responseReady = true;
			return;
		}

		/* If we're sending a load balancing reply. */
		if (_loadBalancer == true) {
			/* XXX Load balance! */

			_responseReady = true;
			return;
		}

		/* If we're sending a 304 not modified response. */
		if (_notModified == true) {
			_generateNotModifiedReply();
			_responseReady = true;
			return;
		}

		/* Otherwise, send contents of file. */
		if (_fileContents == null) {
			System.err.println("File contents should not be null");
			_generateErrorReply("Internal server error",
				Status.INTERNAL_SERVER_ERROR);
			_responseReady = true;
			return;
		}

		_generateReply();
		_responseReady = true;
	}

	private void _mapURLToFile()
		throws IOException, SHTTPRequestException
	{
		String urlName = _request.getURL();
		if (urlName.startsWith("/")) 
			urlName = urlName.substring(1);

		/* If request is seeking load, set load balancer flag and return.*/
		if (urlName.equals("load")) {
			_loadBalancer = true;
			return;
		}

		/* If file is unnamed, find out if user agent is mobile and map
		   correct index.html */
		String fileName = _documentRoot + urlName;
		if (urlName.endsWith("/")) {
			if (_request.isMobile()) {
				fileName = fileName + Const.DOC_MINDEX;
			} else {
				fileName = fileName + Const.DOC_INDEX;
			}
		}

		/* Set content type based on file extension. */
		if (fileName.endsWith(".jpg"))
			_contentType = "image/jpeg";
		else if (fileName.endsWith(".gif"))
			_contentType = "image/gif";
		else if (fileName.endsWith(".html") || fileName.endsWith(".htm"))
			_contentType = "text/html";
		else
			_contentType = "text/plain";

		/* Look for file. */
		_fileInfo = new File(fileName);
		if (!_fileInfo.isFile()) {
			_fileInfo = null;
			throw new SHTTPRequestException("Not found", Status.NOT_FOUND);
		}

		/* If isModifiedSince is set and file's last modified time is before
		   isModifiedSince, then set flag and return. */
		if (_request.getIfModifiedSince() != null) {
			long requestTime = _request.getIfModifiedSince().getTime();
			long fileTime = _fileInfo.lastModified();
			if (fileTime < requestTime) {
				_notModified = true;
				return;
			}
		}

		/* If file is found in cache (by name), then just get file contents
		   from server cache. */
		ServerCacheFile cacheFile = _serverCache.getFile(fileName);
		if (cacheFile != null) {
			_fileContents = cacheFile.content();
			return;
		}

		/* If file is not executible, get contents of file and return.  */
		if (!_fileInfo.canExecute()) {
			_fileContents = new byte[(int) _fileInfo.length()];
			InputStream in = new FileInputStream(_fileInfo);
			in.read(_fileContents);

			_serverCache.putFile(fileName, _fileInfo);
			return;
		}

		/* Handle an executable file. */
		Process proc = Runtime.getRuntime().exec(fileName);
		BufferedReader procOut = new BufferedReader(new InputStreamReader(
			proc.getInputStream()));
		StringBuffer procOutSb = new StringBuffer();
		String line;
		while ((line = procOut.readLine()) != null)
			procOutSb.append(line);
		procOut.close();
		try {
			proc.waitFor();
		} catch (InterruptedException ie) {
			Debug.DEBUG("Interrupted while waiting for proc to die: " +
				ie.getMessage());
		}

		String contentsString = procOutSb.toString();
		_fileContents = contentsString.getBytes("US-ASCII");
	}

	private void _generateErrorReply(String message, int errorCode)
	{
		_response = new SHTTPResponse();
		_response.setStatus(errorCode, message);
		_response.setServerName(SHTTPAsyncServer.SERVER_NAME);
		_response.setContent(
			"<html>" +
			"<h1>" + errorCode + ": " + message + "</h1>" +
			"</html>");

		try {
			_outBuffer = _response.toByteBuffer();
		} catch (UnsupportedEncodingException uee) {
			System.err.println("Wtf? UnsupportedEncodingException: " +
				uee.getMessage());
		}
	}

	private void _generateNotModifiedReply()
	{
		_response = new SHTTPResponse();
		_response.setStatus(Status.NOT_MODIFIED, "Not modified");
		_response.setContent("File not modified");
		_response.setServerName(SHTTPAsyncServer.SERVER_NAME);

		try {
			_outBuffer = _response.toByteBuffer();
		} catch (UnsupportedEncodingException uee) {
			System.err.println("Wtf? UnsupportedEncodingException: " +
				uee.getMessage());
		}
	}

	private void _generateLoadReply(int status)
	{
		_response = new SHTTPResponse();
		_response.setStatus(status, "Load status");
		_response.setContent("Load status: " + status);
		_response.setServerName(SHTTPAsyncServer.SERVER_NAME);
		
		try {
			_outBuffer = _response.toByteBuffer();
		} catch (UnsupportedEncodingException uee) {
			System.err.println("Wtf? UnsupportedEncodingException: " +
				uee.getMessage());
		}
	}
	
	private void _generateReply()
	{
		_response = new SHTTPResponse();

		_response.setStatus(Status.OK, "Document follows");
		try {
			_response.setContent(new String(_fileContents, "US-ASCII"));
		} catch (UnsupportedEncodingException uee) {
			System.err.println("wtf? UnsupportedEncodingException");
			_generateErrorReply("Internal server error: wtf? encoding?",
				Status.INTERNAL_SERVER_ERROR);
			return;
		}
		_response.setServerName(SHTTPAsyncServer.SERVER_NAME);
		_response.setContentType(_contentType);

		try {
			_outBuffer = _response.toByteBuffer();
		} catch (UnsupportedEncodingException uee) {
			System.err.println("Wtf? UnsupportedEncodingException: " +
				uee.getMessage());
		}
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

		int bytesWritten = _client.write(_outBuffer);
		Debug.DEBUG("--> handleWrite(): wrote(" + bytesWritten + 
			"): " + _outBuffer);

		if (_responseReady && (_outBuffer.remaining() == 0))
			_responseSent = true;

		_updateDispatcher();

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
			_dispatcher.deregisterSelection(selectionKey);
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

		_dispatcher.updateInterests(selectionKey, nextState);
		Debug.DEBUG("--> DONE  Update dispatcher.");
	}
}
