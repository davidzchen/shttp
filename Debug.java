public class Debug {
	private static boolean DEBUG = true;

	public static void DEBUG(String s)
	{
		if (DEBUG)
			System.out.println(s);
	}

	public static void WARN(String s)
	{
		if (DEBUG)
			System.err.println(s);
	}
}
