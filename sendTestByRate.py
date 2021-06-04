#!/usr/bin/python
import random
import numpy
import sys
from time import sleep

exit=0
numberOfLines=int(sys.argv[1])
fileToSend=sys.argv[2]
with open (fileToSend, 'r') as test:
	while True:
	    for i in range(numberOfLines):
	    	line = test.readline()
	    	if not line:
			exit=1
			break
		print line.rstrip()
		sys.stdout.flush()
	    if exit==1:
		test.seek(0)
	    sleep(1)
