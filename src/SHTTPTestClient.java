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
	public static boolean _clientRunning;

	/* Private variables for threads */	
	private InetAddress __serverIPAddress;
	private int __serverPort;
	private int __filesDownloaded;
	private int __filesRequested;
	private long __bytesDownloaded;

	public SHTTPTestClient(InetAddress serverIPAddress, int serverPort)
	{
		super();

		__serverIPAddress = serverIPAddress;
		__serverPort      = serverPort;
		__filesDownloaded = 0;
		__filesRequested  = 0;
		__bytesDownloaded = 0;
	}

	public void run()
	{
		while (true) {

			for (String file : _files) {
				if (_clientRunning == false) {
					// Callback
				}

				Socket clientSocket = new Socket(__serverIPAddress, 
					__serverPort);
			
				SHTTPRequest request;
				try {
					request = new SHTTPRequest(file);
				} catch (Exception e) {
					System.err.println("Cannot create request: " + 
						e.getMessage());
					continue;
				}
				__filesRequested++;

				DataOutputStream outToServer;
				
				try {
					outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
					outToServer.writeBytes(request.getBytes());
				} catch (Exception e) {
					System.err.println("Cannot write to server: " +
						e.getMessage());
					continue;
				}

				BufferedReader inFromServer;
				SHTTPResponse response;
				try {
					inFromServer = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
					response = new SHTTPResponse(inFromServer);
				} catch (UnsupportedEncondingException uee) {
					System.err.println("Unsupported encoding for response: " +
						uee.getMessage());
					continue;
				} catch (Exception e) {
					System.err.println("Cannot read response from server: " +
						e.getMessage());
					continue;
				}

				__filesDownloaded++;
				__bytesDownloaded += response.getBytesRead();

				clientSocket.close();
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
		if (args != 10) {
			_usage();
			System.exit(1);
		}

		for (int i = 0; i < args.length; i += 2) {
			if (i == args.length - 1) {
				_usage();
				System.exit(1);
			}

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
		
		_files = files.toArray();

		in.close();
	}

	public static void main(String[] args)
	{
		_parseOptions(args);

		try {
			_readFile();
		} catch (Exception e) {
			System.err.println("Cannot read file " + _filename + ": " +
				e.getMessage());
			System.exit(2);
		}

		InetAddress serverIPAddress;
		try {
			serverIPAddress = InetAddress.getByName(_server);
		} catch (UnknownHostException uhe) {
			System.err.println("Unknown host: " + _server + "\n");
			System.exit(3);
		} catch (Exception e) {
			System.err.println("Error getting host: " + e.getMessage() + "\n");
			System.exit(3);
		}

		_clientRunning = true;
		for (int i = 0; i < _threads; i++) {
			new SHTTPTestClient(serverIPAddress, _port).start();
		}
		Thread.sleep(_time * 1000);
		_clientRunning = false;
	}
}
