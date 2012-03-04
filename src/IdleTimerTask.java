import java.nio.channels.SelectionKey;

public class IdleTimerTask implements Runnable {

	private Dispatcher _dispatcher;
	private SelectionKey _key;

	public IdleTimerTask(Dispatcher dispatcher, SelectionKey key)
	{
		_dispatcher = dispatcher;
		_key = key;
	}

	public void run()
	{
		_dispatcher.closeChannel(_key);
	}
}
