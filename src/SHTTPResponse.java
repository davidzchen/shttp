import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;

public class SHTTPResponse {

	private int _statusCode;
	private String _message;
	private String _date;
	private String _serverName;
	private String _contentType;
	private int _contentLength;

	private int _totalBytes;

	public static int getTotalBytesRead(BufferedReader in)
		throws IOException
	{
		int bytesRead = 0;
		String line;

		while ((line = in.readLine()) != null) {
			bytesRead += line.length() + 2;
		}

		return bytesRead;
	}

	public SHTTPResponse(BufferedReader in)
		throws UnsupportedEncodingException, IOException
	{
		int bytesRead = 0;
		String line;

		line = in.readLine();
		String[] statusLine = line.split("\\s");
		if (statusLine.length < 3) {
			throw new IOException("Status line invalid");
		}
		_statusCode = Integer.parseInt(statusLine[1]);
		_message = statusLine[2];
		bytesRead += line.length() + 2;

		line = in.readLine();
		String[] dateLine = line.split("\\s");
		if (dateLine.length < 2) {
			throw new IOException("Date line invalid");
		}
		_date = dateLine[1];
		bytesRead += line.length() + 2;

		line = in.readLine();
		String[] serverLine = line.split("\\s");
		if (serverLine.length < 2) {
			throw new IOException("Server line invalid");
		}
		_serverName = serverLine[1];
		bytesRead += line.length() + 2;

		line = in.readLine();
		String[] contentTypeLine = line.split("\\s");
		if (contentTypeLine.length < 2) {
			throw new IOException("Content-type line invalid");
		}
		_contentType = contentTypeLine[1];
		bytesRead += line.length() + 2;

		line = in.readLine();
		String[] contentLengthLine = line.split("\\s");
		if (contentLengthLine.length < 2) {
			throw new IOException("Content-length line invalid");
		}
		System.out.println(line);
		System.out.println("(" + contentLengthLine[1] + ")");
		_contentLength = Integer.parseInt(contentLengthLine[1]);
		bytesRead += line.length() + 2;
		bytesRead += _contentLength + 2;

		_totalBytes = bytesRead + _contentLength;
	}

	public int numBytesRead()
	{
		return _totalBytes;
	}

	public String getMessage()
	{
		return _message;
	}

	public String getServerName()
	{
		return _serverName;
	}

	public String getContentType()
	{
		return _contentType;
	}

	public int getContentLength()
	{
		return _contentLength;
	}

}
