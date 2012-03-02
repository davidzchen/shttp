/*
 * Adapted from:
 * http://www.java2s.com/Code/Java/Development-Class/RFCdateformat.htm
 */

import java.util.*;
import java.text.*;

public class DateHelper {

	public final static Locale LOCALE_US = Locale.US;

	public final static TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

	public final static String RFC1123_DATE_PATTERN =
		"EEE, dd MMM yyyy HH:mm:ss z";

	public final static String RFC1036_DATE_PATTERN =
		"EEEEEEEEE, dd-MMM-yy HH:mm:ss z";

	public final static String ASCTIME_DATE_PATTERN =
		"EEE MMM d HH:mm:ss yyyy";

	public final static DateFormat RFC1123Format =
		new SimpleDateFormat(RFC1123_DATE_PATTERN, LOCALE_US);

	public final static DateFormat RFC1036Format =
		new SimpleDateFormat(RFC1036_DATE_PATTERN, LOCALE_US);

	public final static DateFormat ASCTimeFormat =
		new SimpleDateFormat(ASCTIME_DATE_PATTERN, LOCALE_US);

	static {
		RFC1123Format.setTimeZone(GMT_ZONE);
		RFC1036Format.setTimeZone(GMT_ZONE);
		ASCTimeFormat.setTimeZone(GMT_ZONE);
	}

	public static Date parseHTTPDate(String dateString)
		throws ParseException
	{
		Date d = null;

		try {
			d = (Date) RFC1123Format.parse(dateString);
		} catch (ParseException pe) {
			try {
				d = (Date) RFC1036Format.parse(dateString);
			} catch (ParseException ppe) {
				d = (Date) ASCTimeFormat.parse(dateString);
			}
		}

		return d;
	}

	public static String getHTTPDate(Date date)
	{
		StringBuilder sb = new StringBuilder(RFC1123Format.format(date));

		return sb.toString();
	}
}
