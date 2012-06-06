import socket
import resourcesmgr

def client():
    HOST, PORT = 'localhost', 50700
    # SOCK_STREAM == a TCP socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #sock.setblocking(0)  # optional non-blocking
    sock.connect((HOST, PORT))
    sock.send("W") # we are a worker

    while True:
        command = sock.recv(3)  # limit reply to 16K
        if command == "JAR":
#            print "Updating the local JAR archive"
#            file_size = int(sock.recv(12))
#            file_md5 = sock.recv(32)
#            print "file size: %i md5: %s" % (file_size, file_md5)

#            file_content = sock.recv(file_size)
#            print "Content: %s" % (file_content, )
            jar_file = resourcesmgr.recieve_data(sock)
            print jar_file
        elif command == "CMD":
            print "Recieved processing request"
            print "Recieving the input PDF file"
            pdf_file = resourcesmgr.recieve_file(sock)
            command = recieve_data(sock)
            # now process command using the obtained data
            pass
            results = ""
            # now send results file (compressed results folder)
            resourcesmgr.send_data(results)


    sock.close()
    return reply


client()

