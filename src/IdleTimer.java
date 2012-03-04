import java.util.HashMap;
import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.nio.channels.SelectionKey;

public class IdleTimer implements Runnable {

	private long _incompleteTimeout;

	private Dispatcher _dispatcher;
	private HashMap<SelectionKey, Long> _idleTimers;
	private List<SelectionKey> _cancelRequests;
	private List<SelectionKey> _addRequests;

	public IdleTimer(Dispatcher dispatcher, int incompleteTimeout)
	{
		_dispatcher = dispatcher;
		_idleTimers = new HashMap<SelectionKey, Long>();
		_cancelRequests = new Vector<SelectionKey>();
		_addRequests = new Vector<SelectionKey>();
		_incompleteTimeout = incompleteTimeout;
	}

	public void registerIdleTimer(SelectionKey key)
	{
		synchronized (_addRequests) {
			_addRequests.add(key);
		}
	}

	public void cancelIdleTimer(SelectionKey key)
	{
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
					_idleTimers.put(k, endTime);
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

			for (Map.Entry<SelectionKey, Long> entry : _idleTimers.entrySet()) {
				SelectionKey key = entry.getKey();
				long endTime = entry.getValue();
				long currTime = System.currentTimeMillis();

				if (endTime > currTime) {
					_dispatcher.invokeLater(new IdleTimerTask(_dispatcher, key));
					_idleTimers.remove(key);
				}
			}
		}
	}
}
