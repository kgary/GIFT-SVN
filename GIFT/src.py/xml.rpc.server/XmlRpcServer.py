#!/usr/bin/python
# This script is now compatible with Python 3.x (tested with WinPython-64bit-3.5.4.1Qt5).  
# It was originally developed and tested with WinPython-32bit-2.7.5.1.
# You will need to make sure your Python install contains scipy (WinPython normally does).

import sys, getopt
from xmlrpc.server import SimpleXMLRPCServer

from IncECGdetection import *
from KinectSensorDataProcessor import *
from TC3GameStateDataProcessor import *

#TODO: better error checking for command line args

def main(argv):

   port = 6000
   classname = ''
   
   try:
      opts, args = getopt.getopt(argv,"hp:c:",["port=", "classname="])
   except getopt.GetoptError:
      print ('test.py -p <port> -c <classname>')
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
         print ('test.py -p <port> -c <classname>')
         sys.exit()
      elif opt in ("-p", "--port"):
         port = int(arg)
      elif opt in ("-c", "--classname"):
         classname = arg
   print ('port: ', port)
   print ('classname: ', classname)
   
   #Start the XML RPC server on local host at the port specified via command line args
   server = SimpleXMLRPCServer(("localhost", port))   
   
   server.logRequests = 0
   #server.allow_none = 1

   #Instantiate the class used for this server instance:
   #server.register_instance(GiftQrsDetect())   
   server.register_instance(eval(classname)())
   
   server.register_function(lambda astr: '_' + astr, '_string')
   server.serve_forever()

if __name__ == "__main__":
   main(sys.argv[1:])



