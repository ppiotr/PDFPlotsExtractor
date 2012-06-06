# a process managing attached workers and deploying tasks on them

import SocketServer
import hashlib

#class Request

from Queue import Queue

latest_md5 = "" # md5 hash of the latest version of JAR file
latest_jar = "ABCD" * 10 + "F" # the content of the latest JAR file


def send_data(request, file_content):
    #transfer a file over a request object
    print "File transfer started"
    chunk_size = 3 # the size of single sending
    file_len = len(file_content)
    request.send("%012i" % ( file_len))
    request.send(hashlib.md5(file_content).hexdigest())
    # now sending the file in chunks
    sent = 0
    while sent != file_len:
        sent_to = sent + chunk_size
        if sent_to > file_len:
            sent_to = file_len
        print "Sending range [%i:%i]" % ( sent, sent_to)
        request.send(file_content[sent: sent_to])
        sent = sent_to

def recieve_data(request):
    file_size = int(request.recv(12))
    file_md5 = request.recv(32)
    print("Recieving file of size %i and md5 %s" % (file_size, file_md5))
    chunk_size = 2
    recieved = 0
    parts = []
    while recieved != file_size:
        if recieved + chunk_size > file_size:
            # decrease last chunk size
            chunk_size = file_size - recieved
        print "Trying to read chunk of size %i" % (chunk_size, )

        new_part = request.recv(chunk_size)
        parts.append(new_part)
        recieved += len(new_part)
        print "reciever part of length %i" % (len(new_part))

    file_content = "".join(parts)

    print "Expected md5: %s actual md5: %s " % (file_md5, hashlib.md5(file_content).hexdigest())
    return file_content


class Worker():
    # processign of a single worker
    def __init__(self, request):
        self.specialQueue = Queue()
        self.jar_md5 = ""
        self.request = request


    def update_jar_if_necessary(self):
        """Update the jar archive"""
        if self.jar_md5 != latest_md5:
            self.request.send("")




class ClientRequestHandler(SocketServer.BaseRequestHandler ):
    def setup(self):
        client_type = self.request.recv(1)

        if client_type == "W":
            print "Worker connected at " + str(self.client_address)
        elif client_type == "C":
            print "Controller connected at " + str(self.client_address)
            # we can have only a single controller !
        else:
            print "ERROR: unknown type of client connected"

        self.request.send("JAR")
        send_data(self.request, latest_jar)

    def handle(self):
        data = 'dummy'
        while data:
            data = self.request.recv(1024)
            self.request.send(data)
            if data.strip() == 'bye':
                return

    def finish(self):
        print self.client_address, 'disconnected!'
        self.request.send('bye ' + str(self.client_address) + '\n')

    #server host is a tuple ('host', port)
if __name__=="__main__":
    server = SocketServer.ThreadingTCPServer(('', 50700), ClientRequestHandler)
    server.serve_forever()
