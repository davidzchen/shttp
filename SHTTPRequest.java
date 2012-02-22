import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;

public class SHTTPRequest {

	private String _filename;

	public SHTTPRequest(String filename)
	{
		_filename = filename;
	}

	public byte[] getBytes()
		throws UnsupportedEncodingException
	{
		ByteBuffer bb = ByteBuffer.allocate(17 + _filename.length());

		bb.put(Constants.M_GET.getBytes("US-ASCII"));
		bb.put(Constants.B_SPACE);
		bb.put(_filename.getBytes("US-ASCII"));
		bb.put(Constants.B_SPACE);
		bb.put(Constants.HTTP_VER.getBytes("US-ASCII"));
		bb.put(Constants.B_CR).put(Constants.B_LF);
		bb.put(Constants.B_CR).put(Constants.B_LF);

		return bb.array();
	}

	public String getString()
		throws UnsupportedEncodingException
	{
		return new String(getBytes(), "US-ASCII");
	}

}
