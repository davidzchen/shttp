import java.io.*;
import java.net.*;
import java.util.*;

class ServerCache {

	private HashMap<String, File> _cacheStore;
	private long _bytesStored;
	private long _bytesMax;

	public ServerCache(int cacheSize)
	{
		_bytesMax = (long) (cacheSize * 1000);
		_bytesStored = 0;
		_cacheStore = new HashMap<String, File>();
	}

	public File getFile(String key)
	{
		// XXX
		return null;
	}
}
