import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.ParseException;

public class SHTTPResponse {

	private int _statusCode;
	private String _message;
	private String _date;
	private String _serverName;
	private String _contentType;
	private Date _lastModified;
	private int _contentLength;

	private String _content;

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

	public SHTTPResponse()
	{
		_statusCode    = 0;
		_message       = null;
		_date          = null;
		_serverName    = null;
		_contentType   = null;
		_lastModified  = null;
		_contentLength = 0;
	}

	public SHTTPResponse(BufferedReader in)
		throws UnsupportedEncodingException, IOException
	{
		int bytesRead = 0;
		String line;
		
		StringBuffer sb = new StringBuffer();
		int termCount = 0;
		int c;
		while ((c = in.read()) != -1) {
			sb.append(c);
			bytesRead++;

			if (c == '\r') {
				if (termCount == 0 || termCount == 2)
					termCount++;
			} else if (c == '\n') {
				if (termCount == 1) {
					termCount++;
				} else {
					termCount = 0;
					break;
				}
			} else {
				termCount = 0;
			}
		}

		StringBuffer contentSb = new StringBuffer();
		while ((line = in.readLine()) != null) {
			contentSb.append(line);
			bytesRead += line.length();
		}

		_content = sb.toString();
		String raw = sb.toString();
		String[] lines = raw.split("\r\n");

		String[] statusLine = lines[0].split("\\s");
		if (statusLine.length < 3) {
			throw new IOException("Status line invalid");
		}
		_statusCode = Integer.parseInt(statusLine[1]);
		_message = statusLine[2];

		for (String line2 : lines) {
			if (line2.length() == 0)
				break;

			String[] parts = line2.split(":");
			if (parts.length < 2) {
				throw new IOException("Invalid header: " + line2);
			}

			if (parts[0].equals("Date")) {
				_date = parts[1].trim();
			} else if (parts[0].equals("Server")) {
				_serverName = parts[1].trim();
			} else if (parts[0].equals("Content-Type")) {
				_contentType = parts[1].trim();
			} else if (parts[0].equals("Content-Length")) {
				_contentLength = Integer.parseInt(parts[1].trim());
			} else if (parts[0].equals("Last-Modified")) {
				try {
					setLastModified(parts[1].trim());
				} catch (ParseException pe) {
					_lastModified = null;
				}
			} else {
				continue;
			}
		}

		_totalBytes = bytesRead;
	}

	public void writeToStream(DataOutputStream out)
		throws IOException
	{
		out.writeBytes("HTTP/1.0 " + _statusCode + " " + _message + "\r\n");

		if (_serverName != null)
			out.writeBytes("Server: " + _serverName + "\r\n");
		if (_date != null)
			out.writeBytes("Date: " + _date + "\r\n");
		if (_lastModified != null)
			out.writeBytes("Last-Modified: " + 
				DateHelper.getHTTPDate(_lastModified) + "\r\n");
		if (_contentType != null)
			out.writeBytes("Content-Type: " + _contentType + "\r\n");
		if (_content != null) {	
			out.writeBytes("Content-Length: " + _contentLength + "\r\n");
			out.writeBytes("\r\n");
			out.writeBytes(_content);
		}
	}

	public int numBytesRead()
	{
		return _totalBytes;
	}

	public int getStatusCode()
	{
		return _statusCode;
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

	public Date getLastModified()
	{
		return _lastModified;
	}

	public void setStatus(int status, String message)
	{
		_statusCode = status;
		_message = message;
	}

	public void setLastModified(long timestamp)
	{
		_lastModified = new Date(timestamp);
	}

	public void setLastModified(String dateString)
		throws ParseException
	{
		_lastModified = DateHelper.parseHTTPDate(dateString);
	}

	public void setServerName(String server)
	{
		_serverName = server;
	}

	public void setContentType(String contentType)
	{
		_contentType = contentType;
	}

	public void setContent(String content)
	{
		_content = content;
		_contentLength = content.length();
	}

	public void print()
	{
		System.out.println("== Begin SHTTP Response ==");
		System.out.println("        Server: " + _serverName);
		System.out.println("        Status: " + _statusCode);
		System.out.println("          Date: " + _date);
		System.out.println("  Content-Type: " + _contentType);
		System.out.println(" Last-Modified: " + _lastModified);
		System.out.println("Content-Length: " + _contentLength);
		System.out.println("Content:");
		System.out.println(_content);
		System.out.println("== End SHTTP Response ==");
	}

}
