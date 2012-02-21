import java.io.*;
import java.util.*;

public class SHTTPResponse {

	private int _statusCode;
	private String _message;
	private String _date;
	private String _serverName;
	private String _contentType;
	private int _contentLength;

	private int _totalBytes;

	public SHTTPResponse(BufferedReader in)
		throws UnsupportedEncodingException
	{
		byte[] bytes;
		int bytesRead = 0;

		bytes = in.readLine().getBytes();
		_statusCode = Arrays.copyOfRange(bytes, 9, 12);
		_message = new String(Arrays.copyOfRange(bytes, 13, bytes.length),
			"US-ASCII");
		bytesRead += bytes.length + 1;

		bytes = in.readLine().getBytes();
		_date = new String(Arrays.copyOfRange(bytes, 6, bytes.length),
			"US-ASCII");
		bytesRead += bytes.length + 1;

		bytes = in.readLine().getBytes();
		_serverName = new String(Arrays.copyOfRange(bytes, 8, bytes.length),
			"US-ASCII");
		bytesRead += bytes.length + 1;

		bytes = in.readLine().getBytes();
		_contentType = new String(Arrays.copyOfRange(bytes, 14, bytes.length),
			"US-ASCII");
		bytesRead += bytes.length + 1;

		bytes = in.readLine.getBytes();
		_contentLength = Integer.parseInt(Arrays.copyOfRange(bytes,
			16, bytes.length));
		bytesRead += bytes.length + 1;

		/* Adjust for CRLF */
		bytesRead += 2;

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
