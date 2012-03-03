import java.io.*;
import java.util.*;

public class ServerConfigException extends Exception {

	private String _message;
	private String _filename;
	private String _line;
	private int _lineNumber;
	private int _tokenStart;
	private int _tokenEnd;
	private boolean _parserPrintable;

	public ServerConfigException(String message)
	{
		super(message);

		_parserPrintable = false;
	}

	public ServerConfigException(String message, String filename, String line, 
		int lineNumber, int tokenStart, int tokenEnd)
	{
		super(message);

		_message    = message;
		_filename   = filename;
		_line       = line;
		_lineNumber = lineNumber;
		_tokenStart = tokenStart;
		_tokenEnd   = tokenEnd;

		_parserPrintable = true;
	}

	public void printParserMessage()
	{
		System.err.println(_message);
		System.err.println("Error in " + _filename + " at line " + 
			_lineNumber + ":");
		System.err.println();
		System.err.println(_line);
		for (int i = 0; i < _tokenStart; i++)
			System.err.print(" ");
		for (int i = _tokenStart; i < _tokenEnd; i++)
			System.err.print("^");
		System.err.println();
	}
}
