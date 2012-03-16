CGI_OBJ += src/www/test.cgi

src/www/test.cgi: src/www/test_cgi.c
	gcc $< -o $@

