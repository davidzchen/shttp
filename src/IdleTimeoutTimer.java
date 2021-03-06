import java.util.HashMap;
import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.channels.SelectionKey;

public class IdleTimeoutTimer implements Runnable {

	private long _incompleteTimeout;

	private Dispatcher _dispatcher;
	private HashMap<SelectionKey, IdleTimer> _idleTimers;
	private List<SelectionKey> _cancelRequests;
	private List<SelectionKey> _addRequests;

	public IdleTimeoutTimer(Dispatcher dispatcher, int incompleteTimeout)
	{
		_dispatcher = dispatcher;
		_idleTimers = new HashMap<SelectionKey, IdleTimer>();
		_cancelRequests = new Vector<SelectionKey>();
		_addRequests = new Vector<SelectionKey>();
		_incompleteTimeout = incompleteTimeout;
	}

	public void registerIdleTimer(SelectionKey key)
	{
		Debug.WARN(" ~~ Register idle timer for key: " + key);
		synchronized (_addRequests) {
			_addRequests.add(key);
		}
	}

	public void cancelIdleTimer(SelectionKey key)
	{
		Debug.WARN(" ~~ Cancel idle timer for key: " + key);
		synchronized (_cancelRequests) {
			_cancelRequests.add(key);
		}
	}

	public void run()
	{
		while (true) {
			synchronized (_addRequests) {
				for (int i = 0; i < _addRequests.size(); i++) {
					SelectionKey k = _addRequests.get(i);
					long endTime = System.currentTimeMillis() + 
						_incompleteTimeout;
					_idleTimers.put(k, new IdleTimer(endTime));
				}
				_addRequests.clear();
			}

			synchronized (_cancelRequests) {
				for (int i = 0; i < _cancelRequests.size(); i++) {
					SelectionKey k = _cancelRequests.get(i);
					if (_idleTimers.containsKey(k))
						_idleTimers.remove(k);
				}
				_cancelRequests.clear();
			}

			Set<Map.Entry<SelectionKey, IdleTimer>> entries = 
				_idleTimers.entrySet();

			for (Map.Entry<SelectionKey, IdleTimer> entry : entries) {
				SelectionKey key = entry.getKey();
				IdleTimer timer = entry.getValue();
				long currTime = System.currentTimeMillis();

				if (timer.isActive() && currTime > timer.getEndTime()) {
					Debug.WARN(" ~~ Attempt to close channel for key: " + 
						key);
					_dispatcher.invokeLater(new IdleTimerTask(_dispatcher, 
						this, key));
					timer.setInactive();
				}
			}
		}
	}
}
