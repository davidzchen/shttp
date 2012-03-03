
public class Status {

	/* 1xx Informational. */
	public final static int INFO = 100;

	/* 2xx Successful */
	public final static int OK = 200;
	public final static int CREATED = 201;
	public final static int ACCEPTED = 202;
	public final static int NO_CONTENT = 204;

	/* 3xx Redirection */
	public final static int MULTIPLE_CHOICES = 300;
	public final static int MOVED_PERMANENTLY = 301;
	public final static int NOT_MODIFIED = 304;

	/* 4xx Client error */
	public final static int BAD_REQUEST = 400;
	public final static int UNAUTHORIZED = 401;
	public final static int FORBIDDEN = 403;
	public final static int NOT_FOUND = 404;

	/* 5xx Server error */
	public final static int INTERNAL_SERVER_ERROR = 500;
	public final static int NOT_IMPLEMENTED = 501;
	public final static int BAD_GATEWAY = 502;
	public final static int SERVICE_UNAVAILABLE = 503;

}
