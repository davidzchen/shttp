import java.nio.channels.*;
import java.io.IOException;
import java.util.*;

public class Dispatcher implements Runnable {

	private Thread _dispatcherThread;

	private ServerCache _serverCache;
	private ServerConfig _config;
	private Selector _selector;

	private List<Runnable> _pendingInvocations;

	public Dispatcher(ServerCache serverCache, ServerConfig config) 
	{
		_config = config;
		_serverCache = serverCache;

		_pendingInvocations = new ArrayList<Runnable>(32);

		try {
			_selector = Selector.open();
		} catch (IOException ie) {
			System.out.println("Cannot create selector: " +
				ie.getMessage());
			ie.printStackTrace();
			System.exit(1);
		}
	}

    public SelectionKey registerNewSelection(SelectableChannel channel, 
		IChannelHandler handler, int ops) 
		throws ClosedChannelException
	{
		SelectionKey key = channel.register(_selector, ops);
		key.attach(handler);
		return key;
	}

	public SelectionKey keyFor(SelectableChannel channel)
	{
		return channel.keyFor(_selector);
	}

	public void deregisterSelection(SelectionKey key)
		throws IOException
	{
		key.cancel();
	}

	public void updateInterests(SelectionKey sk, int newOps)
	{
		sk.interestOps(newOps);
	}

	public void setThread(Thread thread)
	{
		_dispatcherThread = thread;
	}

	public void invokeLater(Runnable task)
	{
		synchronized (_pendingInvocations) {
			_pendingInvocations.add(task);
		}
		_selector.wakeup();
	}

	public void closeChannel(SelectionKey key)
	{
		try {
			key.channel().close();
			deregisterSelection(key);
		} catch (IOException ie) {
			System.err.println("Error closing channel associated with key: " +
				ie.getMessage());
		}
	}

	public void invokeAndWait(final Runnable task)
		throws InterruptedException
	{
		if (Thread.currentThread() == _dispatcherThread) {
			task.run();
		} else {
			final Object latch = new Object();
			synchronized (latch) {
				this.invokeLater(new Runnable() {
					public void run()
					{
						task.run();
						latch.notify();
					}
				});
				latch.wait();
			}
		}
	}

	private void _doInvocations()
	{
		synchronized (_pendingInvocations) {
			for (int i = 0; i < _pendingInvocations.size(); i++) {
				Runnable task = (Runnable) _pendingInvocations.get(i);
				task.run();
			}
			_pendingInvocations.clear();
		}
	}

	public void run()
	{
		while (true) {
			Debug.DEBUG("Enter selection");

			_doInvocations();

			try {
				_selector.select();
			} catch (IOException ie) {
				System.err.println("Error during selection: " +
					ie.getMessage());
				ie.printStackTrace();
				break;
			}

			Set readyKeys = _selector.selectedKeys();
			Iterator iterator = readyKeys.iterator();

			while (iterator.hasNext()) {
				
				SelectionKey key = (SelectionKey) iterator.next();
				iterator.remove();

				try {
					/* Accept a ready new connection. */
					if (key.isAcceptable()) {
						IAcceptHandler aH = (IAcceptHandler) key.attachment();
						aH.handleAccept(key);
					}

					if (key.isReadable() || key.isWritable()) {
						IReadWriteHandler rwH = (IReadWriteHandler) 
							key.attachment();

						if (key.isReadable()) {
							rwH.handleRead(key);
						}

						if (key.isWritable()) {
							rwH.handleWrite(key);
						}
					}
				} catch (IOException ie) {
					Debug.DEBUG("Error handling key [" + key + "]: " +
						ie.getMessage());
					try {
						key.channel().close();
					} catch (IOException iie) {
						/* Do nothing */
					}
				}
			}
		}
	}
}
