# For building SHTTP
JAVAC = javac
JFLAGS = -g
JSRC = $(wildcard src/*.java)

# For building docs
TEX = pdflatex
TEX_OBJ = doc/dzc2-prog1-report.pdf

.PHONY: all clean jar doc dclean

all: $(JSRC)
	@echo "Building all..."
	@$(JAVAC) $(JFLAGS) $?
	@echo "\033[1;32mDONE\033[0m"

clean:
	rm -rf src/*.class

doc: doc/dzc2-prog1-report.pdf
	$(TEX) $<

dclean:
	rm -rf $(TEX_OBJ)

jar:
	@echo "null"
