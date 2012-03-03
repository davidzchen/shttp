import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.ParseException;

public class SHTTPRequest {

	private String _method;
	private String _url;
	private String _userAgent;
	private Date _ifModifiedSince;
	private boolean _mobile;

	public SHTTPRequest()
	{
		_method = Const.M_GET;
		_url = null;
		_userAgent = null;
		_ifModifiedSince = null;
		_mobile = false;
	}

	public SHTTPRequest(StringBuffer sb)
		throws SHTTPRequestException
	{
		String raw = sb.toString();
		String[] lines = raw.split("\r\n");

		String[] requestLine = lines[0].split("\\s");

		if (requestLine.length < 2) {
			Debug.DEBUG("Bad request line: (" + lines[0] + ")");
			throw new SHTTPRequestException("Bad request", Status.BAD_REQUEST);
		}
		if (!requestLine[0].equals(Const.M_GET)) {
			throw new SHTTPRequestException("Not implemented",
				Status.NOT_IMPLEMENTED);
		}
		_method = requestLine[0];
		_url = requestLine[1];

		for (String line : lines) {
			if (line.length() == 0)
				break;

			String[] parts = line.split(":");
			if (parts[0].equals("User-Agent")) {
				_userAgent = parts[1].trim();
				if (_userAgent.contains("IEMobile"))
					_mobile = true;
			} else if (parts[0].equals("If-Modified-Since")) {
				try {
					setIfModifiedSince(parts[1].trim());	
				} catch (ParseException pe) {
					_ifModifiedSince = null;
				}
			} else {
				continue;
			}
		}
	}

	public SHTTPRequest(BufferedReader in)
		throws IOException, SHTTPRequestException
	{
		StringBuffer sb = new StringBuffer();
		int termCount = 0;
		int c;
		while ((c = in.read()) != -1) {
			sb.append((char) c);
			
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

		String raw = sb.toString();
		String[] lines = raw.split("\r\n");

		String[] requestLine = lines[0].split("\\s");

		if (requestLine.length < 2) {
			Debug.DEBUG("requestLine.length(" + requestLine.length + ") < 2");
			throw new SHTTPRequestException("Bad reqest", Status.BAD_REQUEST);
		}
		if (!requestLine[0].equals(Const.M_GET)) {
			throw new SHTTPRequestException("Not implemented", 
				Status.NOT_IMPLEMENTED);
		}
		_method = requestLine[0];
		_url = requestLine[1];

		for (String line : lines) {
			if (line.length() == 0)
				break;

			String[] parts = line.split(":");
			if (parts[0].equals("User-Agent")) {
				_userAgent = parts[1].trim();
				if (_userAgent.contains("IEMobile"))
					_mobile = true;
			} else if (parts[0].equals("If-Modified-Since")) {
				try {
					setIfModifiedSince(parts[1].trim());
				} catch (ParseException pe) {
					_ifModifiedSince = null;
				}
			} else {
				continue;
			}
		}
	}

	public void writeToStream(DataOutputStream out)
		throws IOException
	{
		out.writeBytes(_method + " " + _url + " " + "HTTP/1.0" + "\r\n");

		if (_userAgent != null)
			out.writeBytes("User-Agent: " + _userAgent + "\r\n");
		if (_ifModifiedSince != null)
			out.writeBytes("If-Modified-Since: " + 
				DateHelper.getHTTPDate(_ifModifiedSince) + "\r\n");

		out.writeBytes("\r\n");
	}

	public void setURL(String url)
	{
		_url = url;
	}

	public void setUserAgent(String userAgent)
	{
		_userAgent = userAgent;
	}

	public void setIfModifiedSince(long timestamp)
	{
		_ifModifiedSince = new Date(timestamp);
	}

	public void setIfModifiedSince(String dateString)
		throws ParseException
	{
		_ifModifiedSince = DateHelper.parseHTTPDate(dateString);
	}

	public String getMethod()
	{
		return _method;
	}

	public String getURL()
	{
		return _url;
	}

	public String getUserAgent()
	{
		return _userAgent;
	}

	public Date getIfModifiedSince()
	{
		return _ifModifiedSince;
	}

	public boolean isMobile()
	{
		return _mobile;
	}

	public void print()
	{
		System.out.println("== Begin SHTTP Request ==");
		System.out.println("           Method: " + _method);
		System.out.println("              URL: " + _url);
		System.out.println("       User-Agent: " + _userAgent);
		System.out.println("           Mobile: " + _mobile);
		System.out.println("If-Modified-Since: " + _ifModifiedSince);
		System.out.println("== End SHTTP Request ==");
	}
}
