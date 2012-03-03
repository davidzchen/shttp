JAVAC = javac
JFLAGS = -g
SRC = $(wildcard src/*.java)

.PHONY: all clean jar

all: $(SRC)
	$(JAVAC) $(JFLAGS) $?

clean:
	rm -rf src/*.class

jar:
	@echo "null"
