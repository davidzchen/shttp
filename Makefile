JAVAC = javac
OBJ = Const.class \
      Debug.class \
      Status.class \
      DateHelper.class \
      SHTTPRequestException.class \
      SHTTPRequest.class \
      SHTTPResponse.class \
      SHTTPTestClientStats.class \
      SHTTPTestClient.class \
      ServerConfigException.class \
      ServerConfig.class \
      ServerCacheFile.class \
      ServerCache.class \
      WebRequestHandler.class \
      ISHTTPSyncServer.class \
      SHTTPSequentialServer.class \
      SHTTPPerRequestServer.class \
      SHTTPPerRequestThread.class \
      SHTTPCompetingServer.class \
      SHTTPCompetingThread.class \
      SHTTPBusyWaitServer.class \
      SHTTPBusyWaitThread.class \
      SHTTPSuspensionServer.class \
      SHTTPSuspensionThread.class \
      IChannelHandler.class \
      IAcceptHandler.class \
      IReadWriteHandler.class \
      ISocketReadWriteHandlerFactory.class \
      Dispatcher.class \
      Acceptor.class \
      SHTTPReadWriteHandlerFactory.class \
      SHTTPReadWriteHandler.class \
      SHTTPAsyncServer.class

all: $(OBJ)

%.class: %.java
	@$(JAVAC)  $<
	@echo "  JAVAC   $<"

clean:
	rm -rf *.class

.PHONY: clean
