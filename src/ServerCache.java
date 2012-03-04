import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class ServerCache {

	private ConcurrentHashMap<String, ServerCacheFile> _cacheStore;
	private long _bytesStored;
	private long _bytesMax;

	public ServerCache(int cacheSize)
	{
		_bytesMax = (long) (cacheSize * 1000);
		_bytesStored = 0;
		_cacheStore = new ConcurrentHashMap<String, ServerCacheFile>();
	}

	public ServerCacheFile getFile(String key)
	{
		return _cacheStore.get(key);
	}

	public synchronized void putFile(String key, File fileInfo)
	{
		if (isFull(fileInfo.length()))
			return;

		if (_cacheStore.containsKey(key))
			return;

		if (!fileInfo.isFile())
			return;

		long length = fileInfo.length();
		byte[] bytes = new byte[(int) length];
		InputStream in;
		
		try {
			in = new FileInputStream(fileInfo);
			in.read(bytes);
		} catch (Exception e) {
			return;
		}

		ServerCacheFile cacheFile = new ServerCacheFile(
			fileInfo.lastModified(), bytes);

		if (_cacheStore.put(key, cacheFile) != null) {
			_bytesStored += length;
		}
	}

	public boolean isFull(long fileBytes)
	{
		return (_bytesStored + fileBytes > _bytesMax);
	}
}
