
OBJ = Constants.class \
      SHTTPRequest.class \
      SHTTPResponse.class \
      SHTTPTestClientStats.class \
      SHTTPTestClient.class


all: $(OBJ)

%.class: %.java
	javac  $<

clean:
	rm -rf *.class

.PHONY: clean
