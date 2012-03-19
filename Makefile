# For building SHTTP
JAVAC = javac
JFLAGS = -g
JSRC = $(wildcard src/*.java)

# For building docs
TEX = pdflatex
TEX_OBJ = dzc2-prog1-report.pdf dzc2-prog1-report.aux dzc2-prog1-report.log

# For building CGIs
CGI_OBJ = 

.PHONY: all clean jar doc dclean cgi

all: $(JSRC)
	@echo "Building all..."
	@$(JAVAC) $(JFLAGS) $?
	@echo "\033[1;32mDONE\033[0m"

# Build Test CGIs nonrecursively
include src/www/Makefile.inc

cgi: $(CGI_OBJ)

clean:
	rm -rf $(CGI_OBJ)
	rm -rf src/*.class

# Build docs
doc: doc/dzc2-prog1-report.tex
	$(TEX) $<

dclean:
	rm -rf $(TEX_OBJ)

jar:
	jar cvf dzc-shttp-0.1.jar src/*.java Makefile dzc2-prog1-report.pdf shttp.conf test/benchmark* test/*.conf README
