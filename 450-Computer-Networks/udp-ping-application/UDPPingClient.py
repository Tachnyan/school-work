# Author: Tachnyan
# CSC 450 Computer Networks
# UDP ping client.
# Pings UDP server with n number of pingAttempts.
# Prints response and round trip time, and prints statistics.

from socket import *
import datetime as dt
import sys

#retunrs time difference in ms.
def getTimeDiff(timeSent, timeRecv):
    timeDiff = dt.timedelta(milliseconds=0)
    timeDiff = timeRecv - timeSent
    return timeDiff

#handle arguments
def handleArgs(args):
    if len(args) < 2 or args[0] == "-h":
        print("Usage: UDPPingClient.py IP PORT NumberOfPings")
        return False
    if len(args) == 3 and int(args[2]) < 0:
        print("NumberOfPings must be greater than 0")
        return False
    return True

#Ping server, then return the response and the round trip time.
def ping(socket, servAddr):
    timeSent = dt.datetime.now()
    msg = "Ping " + str(timeSent)

    socket.sendto(msg.encode('utf-8'), servAddr)
    response = socket.recvfrom(1024)
    timeRecv = dt.datetime.now()
    timeDiff = getTimeDiff(timeSent, timeRecv)

    return (response, timeDiff)

def makeSocket():
    s = socket(AF_INET, SOCK_DGRAM)
    s.setsockopt(IPPROTO_IP,IP_TTL,32)
    s.settimeout(1);
    return s

def printStatistics(serverIP, pingAttempts, responseCount, lostCount, sumTime, minTime, maxTime):
    print(f"Ping statistics for {serverIP}:"+
    f"\n Segments: Sent: {pingAttempts}, Recieved: {responseCount}, Lost: {lostCount} ({(responseCount/pingAttempts)*100:.2f}%)"+
    f"\n Approximate round trip times in ms:"+
    f"\n Minimum = {minTime:.2f}ms, Maximum = {maxTime:.2f}ms, Average = {sumTime/pingAttempts:.2f}")


def main():
    args = sys.argv[1:]
    if not handleArgs(args):
        return

    serverIP = args[0]
    serverPort = int(args[1])
    servAddr = (serverIP, serverPort)
    pingAttempts = 1 if len(args) != 3 else int(args[2]) #if not given number of pingAttempts only do it once.

    UDPSocket = makeSocket()

    #setup statistics variables
    sumTime = 0.0
    responseCount, lostCount = 0,0
    minTime, maxTime = 100,0

    #set an index equal to wanted pingAttempts
    i = pingAttempts
    while True:
        try:
            # Ping and get response, then increase response count.
            response, timeDiff = ping(UDPSocket, servAddr)
            responseCount+=1

            #convert timeDiff into float, then add to sumTime.
            timeDiff = timeDiff/dt.timedelta(milliseconds=1)
            sumTime += timeDiff

            #check max and min
            if timeDiff > maxTime:
                maxTime = timeDiff
            if timeDiff < minTime:
                minTime = timeDiff

            print(f"Reply from {serverIP}: {response[0].decode('utf-8')} time={timeDiff:.2f} ms TTL=1")

        #if no response, up lost count. 
        except:
            lostCount+=1
            print("Request timed out.")

        #Break out of while once we made number of requested pingAttempts
        i -= 1
        if i <= 0:
            break

    printStatistics(serverIP, pingAttempts, responseCount, lostCount, sumTime, minTime, maxTime)


main()
