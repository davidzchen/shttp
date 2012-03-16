#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main(int argc, char **argv)
{
	puts("<html>");
	puts("\t<head>");
	puts("\t\t<title>Test CGI</title>");
	puts("\t</head>");
	puts("\t<body>");
	puts("\t\t<h1>Test CGI</h1>");
	printf("\t\t<p>Current unix time is: %lu\n", time(NULL));
	puts("\t</body>");
	puts("</html>");

	return EXIT_SUCCESS;
}
