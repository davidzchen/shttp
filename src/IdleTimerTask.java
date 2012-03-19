import java.nio.channels.SelectionKey;

public class IdleTimerTask implements Runnable {

	private Dispatcher _dispatcher;
	private IdleTimeoutTimer _idleTimeoutTimer;
	private SelectionKey _key;

	public IdleTimerTask(Dispatcher dispatcher, 
		IdleTimeoutTimer idleTimeoutTimer, SelectionKey key)
	{
		_dispatcher = dispatcher;
		_idleTimeoutTimer = idleTimeoutTimer;
		_key = key;
	}

	public void run()
	{
		_dispatcher.closeChannel(_key);
		_idleTimeoutTimer.cancelIdleTimer(_key);
	}
}
