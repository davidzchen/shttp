#!/usr/bin/env python

f = open('benchmarks.txt', 'r')

benchmark_sequential = open('benchmark_sequential.txt', 'w')
benchmark_perrequest = open('benchmark_perrequest.txt', 'w')
benchmark_busywait   = open('benchmark_busywait.txt', 'w')
benchmark_compete    = open('benchmark_compete.txt', 'w')
benchmark_suspension = open('benchmark_suspension.txt', 'w')
benchmark_async      = open('benchmark_async.txt', 'w')
benchmark_httpd      = open('benchmark_httpd.txt', 'w')

for line in f:
	parts = line.split('\t')

	print parts

	benchmark_sequential.write("%s\t%s\n" % (parts[0], parts[1]))
	benchmark_perrequest.write("%s\t%s\n" % (parts[0], parts[2]))
	benchmark_busywait.write("%s\t%s\n" % (parts[0], parts[3]))
	benchmark_compete.write("%s\t%s\n" % (parts[0], parts[4]))
	benchmark_suspension.write("%s\t%s\n" % (parts[0], parts[5]))
	benchmark_async.write("%s\t%s\n" % (parts[0], parts[6]))
	benchmark_httpd.write("%s\t%s\n" % (parts[0], parts[7][:-1]))

f.close()
benchmark_sequential.close()
benchmark_perrequest.close()
benchmark_busywait.close()
benchmark_compete.close()
benchmark_suspension.close()
benchmark_async.close()
benchmark_httpd.close()
