import java.nio.channels.SocketChannel;

public class SHTTPReadWriteHandlerFactory 
	implements ISocketReadWriteHandlerFactory {

	public IReadWriteHandler createHandler(Dispatcher d, SocketChannel client,
		ServerCache serverCache, String documentRoot, IdleTimer idleTimer)
	{
		return new SHTTPReadWriteHandler(d, client, serverCache,
			documentRoot, idleTimer);
	}
}
