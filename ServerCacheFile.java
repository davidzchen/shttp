import java.io.File;

public class ServerCacheFile {

	private long _lastModified;
	private byte[] _content;

	public ServerCacheFile(long lastModified, byte[] content)
	{
		_lastModified = lastModified;
		_content = content;
	}

	public long lastModified()
	{
		return _lastModified;
	}

	public byte[] content()
	{
		return _content;
	}

}
