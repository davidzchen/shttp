import java.io.*;
import java.util.*;

public class ServerConfig {

	public final static String T_LISTEN = "Listen";
	public final static String T_DOCUMENT_ROOT = "DocumentRoot";
	public final static String T_THREAD_POOL_SIZE = "ThreadPoolSize";
	public final static String T_CACHE_SIZE = "CacheSize";
	public final static String T_INCOMPLETE_TIMEOUT = "IncompleteTimeout";

	public final static int DEFAULT_LISTEN_PORT = 7980;
	public final static String DEFAULT_DOCUMENT_ROOT = "./";
	public final static int DEFAULT_THREAD_POOL_SIZE = 4;
	public final static int DEFAULT_CACHE_SIZE = 5;
	public final static int DEFAULT_INCOMPLETE_TIMEOUT = 3;

	private String _filename;
	private int _listenPort;
	private String _documentRoot;
	private int _threadPoolSize;
	private int _cacheSize;
	private int _incompleteTimeout;

	public ServerConfig(String filename)
		throws ServerConfigException, FileNotFoundException, IOException
	{
		FileInputStream fstream = new FileInputStream(filename);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		int n = 0;

		_listenPort        = DEFAULT_LISTEN_PORT;
		_documentRoot      = DEFAULT_DOCUMENT_ROOT;
		_threadPoolSize    = DEFAULT_THREAD_POOL_SIZE;
		_cacheSize         = DEFAULT_CACHE_SIZE;
		_incompleteTimeout = DEFAULT_INCOMPLETE_TIMEOUT;

		while ((line = br.readLine()) != null) {
			n++;

			if (line.length() == 0)
				continue;
			if (line.charAt(0) == '#')
				continue;

			String[] parts = line.split("\\s");
			if (parts.length != 2) {
				throw new ServerConfigException(
					"Malformed configuration directive", filename, line,
					n, 0, line.length());
			}

			if (parts[0].equals(T_LISTEN)) {
				_listenPort = Integer.parseInt(parts[1]);
			} else if (parts[0].equals(T_DOCUMENT_ROOT)) {
				_documentRoot = parts[1];
			} else if (parts[0].equals(T_THREAD_POOL_SIZE)) {
				_threadPoolSize = Integer.parseInt(parts[1]);
			} else if (parts[0].equals(T_CACHE_SIZE)) {
				_cacheSize = Integer.parseInt(parts[1]);
			} else if (parts[0].equals(T_INCOMPLETE_TIMEOUT)) {
				_incompleteTimeout = Integer.parseInt(parts[1]) * 1000;
			} else {
				throw new ServerConfigException(
					"Invalid configuration directive", filename, line,
					n, 0, parts[0].length());
			}
		}
	}


	public int listenPort()
	{
		return _listenPort;
	}

	public String documentRoot()
	{
		return _documentRoot;
	}

	public int threadPoolSize()
	{
		return _threadPoolSize;
	}

	public int cacheSize()
	{
		return _cacheSize;
	}

	public int incompleteTimeout()
	{
		return _incompleteTimeout;
	}

	public void print()
	{
		System.out.println("       Listen port: " + _listenPort);
		System.out.println("     Document root: " + _documentRoot);
		System.out.println("  Thread pool size: " + _threadPoolSize);
		System.out.println("        Cache size: " + _cacheSize);
		System.out.println("Incomplete timeout: " + _incompleteTimeout);
	}
}
