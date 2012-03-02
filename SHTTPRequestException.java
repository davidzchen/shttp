import java.util.*;

public class SHTTPRequestException extends Exception {

	private int _errorCode;

	public SHTTPRequestException(String message)
	{
		super(message);
	}

	public SHTTPRequestException(String message, int errorCode)
	{
		super(message);
		_errorCode = errorCode;
	}

	public int getErrorCode() 
	{
		return _errorCode;
	}
}
