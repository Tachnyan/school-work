# Author: Tachnyan
# Class: CSC 450 - Computer Networks
# TCP server that listens for HTTP file requests, and returns a response.

from socket import *
import sys

def HTTP(message): #Determine if HTTP request is valid. 
    msg = message.split()
    if msg[0] != 'GET':
        return "HTTP/1.1 400 BAD REQUEST"

    if msg[2] != 'HTTP/1.1':
        return "HTPP/1.1 400 BAD REQUEST"

    return "HTTP/1.1 200 OK\r\n"

def main():
    args = sys.argv[1:]
    if len(args) != 1 or args[0] == "-h":
        print("Usage: server.py ServerPort")
        return

    serverPort = int(args[0])
    serverSocket = socket(AF_INET,SOCK_STREAM)
    serverSocket.bind(('',serverPort))
    serverSocket.listen(1)
    print('The server is ready to recieve')

    while True:
        (connectionSocket, addr) = serverSocket.accept()

        try:
            #Get message.
            message = connectionSocket.recv(1024).decode()
            print("From client: "+message)

            #Make response message
            response = HTTP(message)
            print("Prepared Response: "+response)

            #Determine requested file, then read it in. 
            filename = message.split()[1]
            f = open(filename[1:])
            outputdata = f.read(-1)

            #send response
            connectionSocket.send(response.encode())
            if response.split()[1] == "400": #if response contains 400 code break out of try.
                break

            for i in range(0, len(outputdata)): #send all data.
                connectionSocket.send(outputdata[i].encode())

            connectionSocket.send("\r\n".encode()) #send TCP header end.

        #If file is not found, respond with 404.
        except IOError:
            response = 'HTTP/1.1 404 Not Found'
            print(response)
            connectionSocket.send(response.encode())

        #close socket.
        connectionSocket.shutdown(0)
        connectionSocket.close()


main()
