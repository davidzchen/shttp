# For building SHTTP
JAVAC = javac
JFLAGS = -g
JSRC = $(wildcard src/*.java)

# For building docs
TEX = pdflatex
TEX_OBJ = dzc2-prog1-report.pdf dzc2-prog1-report.aux dzc2-prog1-report.log

.PHONY: all clean jar doc dclean

all: $(JSRC)
	@echo "Building all..."
	@$(JAVAC) $(JFLAGS) $?
	@echo "\033[1;32mDONE\033[0m"

clean:
	rm -rf src/*.class

doc: doc/dzc2-prog1-report.tex
	$(TEX) $<

dclean:
	rm -rf $(TEX_OBJ)

jar:
	jar cvf shttp-0.1.jar src/* shttp.conf
