import java.io.*;
import java.util.*;
import java.net.*;

public class SHTTPTestClient extends Thread {

	public static String _server;
	public static int _port;
	public static int _threads;
	public static String _filename;
	public static int _time;
	public static String[] _files;
	public static long _clientEndTime;
	public static boolean _test;

	public static SHTTPTestClientStats _clientStats;

	/* Private variables for threads */	
	private InetAddress __serverIPAddress;
	private int __serverPort;

	public static void incrFilesDownloadedCallback(int bytesDownloaded)
	{
		synchronized (_clientStats) {
			_clientStats.incrFilesDownloaded();
			_clientStats.incrBytesDownloaded(bytesDownloaded);
		}
	}

	public static void incrWaitTimeCallback(long waitTime)
	{
		synchronized (_clientStats) {
			_clientStats.incrWaitTime(waitTime);
		}
	}

	public SHTTPTestClient(InetAddress serverIPAddress, int serverPort)
	{
		super();

		__serverIPAddress = serverIPAddress;
		__serverPort      = serverPort;
	}

	public void run()
	{
runloop:
		while (true) {
			for (String file : _files) {

				long currTime = System.currentTimeMillis();
				if (currTime > _clientEndTime)
					break runloop;

				Socket clientSocket;
				try {
					clientSocket = new Socket(__serverIPAddress, 
						__serverPort);
				} catch (IOException ioe) {
					System.out.println("Cannot create socket to server: " +
						ioe.getMessage());
					continue;
				}

				try {
					clientSocket.setSoTimeout((int) (_clientEndTime - currTime));
				} catch (SocketException se) {
					System.out.println("Cannot set socket timeout to " +
						(_clientEndTime - currTime) + ": " + se.getMessage());
					continue;
				}
			
				SHTTPRequest request = new SHTTPRequest();
				request.setURL(file);

				DataOutputStream outToServer;
				try {
					outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
					request.writeToStream(outToServer);
				} catch (IOException ie) {
					System.out.println("Error opening output stream to server: " +
						ie.getMessage());
					continue;
				}
				long requestTime = System.currentTimeMillis();

				BufferedReader inFromServer;
				int bytesRead;
				try {	
					inFromServer = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream(), "US-ASCII"));

					bytesRead = 0;
					String responseLine;
					while ((responseLine = inFromServer.readLine()) != null) {
						if (bytesRead == 0)
							SHTTPTestClient.incrWaitTimeCallback(
								System.currentTimeMillis() - requestTime);

						bytesRead += responseLine.length() + 2;
					}

				} catch (IOException ie) {
					//System.out.println("Error reading from server: " + 
					//	ie.getMessage());
					continue;
				}

				SHTTPTestClient.incrFilesDownloadedCallback(bytesRead);

				try {
					clientSocket.close();
				} catch (IOException ie) {

				}
			}
		}
	}

	private static void _usage()
	{
		System.out.println(
			"Usage: SHTTPTestClient -server <server> -port <server port> \n" +
			"       -parallel <# of threads> -files <file name> \n" +
			"       -T <time of test in seconds>\n");
	}

	private static void _parseOptions(String[] args)
	{
		if (args.length < 10) {
			_usage();
			System.exit(1);
		}

		for (int i = 0; i < args.length; i += 2) {
			if (i == args.length - 1) {
				_usage();
				System.exit(1);
			}

			_test = false;

			if (args[i].equals("-server")) {
				_server = args[i+1];
			} else if (args[i].equals("-port")) {
				_port = Integer.parseInt(args[i+1]);
			} else if (args[i].equals("-parallel")) {
				_threads = Integer.parseInt(args[i+1]);
			} else if (args[i].equals("-files")) {
				_filename = args[i+1];
			} else if (args[i].equals("-T")) {
				_time = Integer.parseInt(args[i+1]);
			} else if (args[i].equals("-test")) {
				if (Integer.parseInt(args[i+1]) == 1)
					_test = true;
			} else {
				System.err.println("Invalid option: " + args[i] + "\n");
				_usage();
				System.exit(1);
			}
		}
	}

	private static void _readFile()
		throws Exception
	{
		FileInputStream fstream = new FileInputStream(_filename);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		ArrayList<String> files = new ArrayList<String>();

		while ((line = br.readLine()) != null)
			files.add(line);
		
		_files = files.toArray(new String[files.size()]);

		in.close();
	}

	public static void main(String[] args)
	{
		_parseOptions(args);
		_clientStats = new SHTTPTestClientStats(_time);

		try {
			_readFile();
		} catch (Exception e) {
			System.err.println("Cannot read file " + _filename + ": " +
				e.getMessage());
			System.exit(2);
		}

		InetAddress serverIPAddress = null;
		try {
			serverIPAddress = InetAddress.getByName(_server);
		} catch (UnknownHostException uhe) {
			System.err.println("Unknown host: " + _server + "\n");
			System.exit(3);
		} catch (Exception e) {
			System.err.println("Error getting host: " + e.getMessage() + "\n");
			System.exit(3);
		}

		Thread[] threads = new Thread[_threads];

		_clientEndTime = System.currentTimeMillis() + (_time * 1000);
		for (int i = 0; i < _threads; i++) {
			threads[i] = new SHTTPTestClient(serverIPAddress, _port);
			threads[i].start();
		}
		try {
			Thread.sleep(_time * 1000);
		} catch (InterruptedException ie) {
			System.err.println("Main thread interrupted: " + ie.getMessage());
			System.exit(5);
		}

		if (_test == false) {
			System.out.println("        Total files downloaded: " + 
				_clientStats.getFilesDownloaded());
			System.out.println("        Total bytes downloaded: " +
				_clientStats.getBytesDownloaded());
			System.out.println("Total transactional throughput: " +
				_clientStats.getTransactionalThroughput() + " files/s");
			System.out.println("          Data rate throughput: " +
				_clientStats.getDataRateThroughput() + " b/s");
			System.out.println("             Average wait time: " +
				_clientStats.getAverageWaitTime() + " ms");
		} else {
			System.out.println(_clientStats.getTransactionalThroughput());
		}
	}
}
