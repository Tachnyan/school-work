# Author: Tachnyan
# CSC 450 Computer Networks
# UDP ping server
# Listens for and responds to UDP socket messages.
# Simulates 40% loss through use of a random number generator.

from socket import *
import datetime
import sys
import random as r

def main():
    args = sys.argv[1:]
    if len(args) < 1 or args[0] == "-h":
        print("Usage: UDPPingServer.py PORT")
        return

    #setup socket
    serverPort = int(args[0])
    serverSocket = socket(AF_INET, SOCK_DGRAM)
    serverSocket.setsockopt(IPPROTO_IP, 12, 32)

    serverSocket.bind(('', serverPort))
    print('Server Listening')

    while True:
        data, addr = serverSocket.recvfrom(1024)

        #random number to simulate packet loss.
        if r.randint(0,9) > 3:
            serverSocket.sendto(data,addr)


main()
