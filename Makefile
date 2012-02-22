JAVAC = javac
OBJ = Constants.class \
      SHTTPRequest.class \
      SHTTPResponse.class \
      SHTTPTestClientStats.class \
      SHTTPTestClient.class \
      ServerConfigException.class \
      ServerConfig.class \
      ServerCache.class \
      WebRequestHandler.class \
      SHTTPSequentialServer.class \
      SHTTPPerRequestThread.class \
      SHTTPPerRequestServer.class \
      SHTTPCompetingThread.class \
      SHTTPCompetingServer.class \
      SHTTPBusyWaitThread.class \
      SHTTPBusyWaitServer.class \
      SHTTPSuspensionThread.class \
      SHTTPSuspensionServer.class \
      SHTTPAsyncServer.class

all: $(OBJ)

%.class: %.java
	$(JAVAC)  $<

clean:
	rm -rf *.class

.PHONY: clean
