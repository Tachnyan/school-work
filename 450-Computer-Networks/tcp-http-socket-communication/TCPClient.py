# Author: Tachnyan
# Class: CSC 450 - Computer Networks
# TCP client that passes HTTP file request and prints response.


from socket import *
import sys

def  HTTP(method, requestTarget, HTTPVersion):
    CRLF = "\r\n"
    SP = " "
    requestLine = method+SP+requestTarget+SP+HTTPVersion+CRLF
    startLine = requestLine

    HTTPMessage =  startLine+CRLF

    return HTTPMessage


def main():
    args = sys.argv[1:]

    if args[0] == "-h" or len(args) < 3:
        print ("Usage: client.py ServerIP ServerPort Requested-Files")
        return

    serverIP = args[0]
    serverPort = int(args[1])
    fileName = args[2]

    clientSocket = socket(AF_INET, SOCK_STREAM)
    clientSocket.connect((serverIP,serverPort))

    message = HTTP("GET", '/'+fileName, "HTTP/1.1" )
    print('Client Request: '+message)
    clientSocket.send(message.encode())
    clientSocket.shutdown(1)

    response = bytearray()
    print("From Server: ")
    while True:
        chunk = clientSocket.recv(2048)
        response += chunk
        if len(chunk) == 0:
            break;

    print(response.decode())
    clientSocket.close()

main()
