# a process managing attached workers and deploying tasks on them

import SocketServer
import hashlib
import tempfile
import cPickle
import os
import base64

#class Request

from Queue import Queue

latest_md5 = "" # md5 hash of the latest version of JAR file
latest_jar = "ABCD" * 10 + "F" # the content of the latest JAR file
current_controller = None
requests_queue = Queue()
results_queue = Queue()


def send_data(request, file_content_raw):
    #transfer a file over a request object
    file_content = base64.b64encode(file_content_raw)

    print "File transfer started"
    chunk_size = 16000 # the size of single sending
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
    chunk_size = 16000
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
    return base64.b64decode(file_content)

#        self.request.send('bye ' + str(self.client_address) + '\n')

    #server host is a tuple ('host', port)
if __name__=="__main__":
    server = SocketServer.ThreadingTCPServer(('', 50717), ClientRequestHandler)
    server.serve_forever()
