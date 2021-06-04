#!/usr/bin/python
import random
import numpy
import sys
from time import sleep

fileToSend=sys.argv[1]
with open (fileToSend, 'r') as test:
	while True:
            line = test.readline()
            if not line:
                break
            print line.rstrip()
	    sys.stdout.flush()
	    sleep(numpy.random.choice(numpy.arange(0, 4), p=[0.5, 0.1, 0.2, 0.2]))
