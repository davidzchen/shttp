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
		ByteBuffer bb = ByteBuffer.allocate();

		bb.put(SHTTPConstants.M_GET.getBytes("US-ASCII"));
		bb.put(SHTTPConstants.B_SPACE);
		bb.put(_filename.getBytes("US-ASCII"));
		bb.put(SHTTPConstants.B_SPACE);
		bb.put(SHTTPConstants.HTTP_VER.getBytes("US-ASCII"));
		bb.put(SHTTPConstants.B_LF);
		bb.put(SHTTPConstants.B_CR).put(SHTTPConstants.B_LF);

		return bb.array();
	}

}
