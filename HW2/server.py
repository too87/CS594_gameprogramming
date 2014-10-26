import asyncore
import socket
import time

class EchoHandler(asyncore.dispatcher_with_send):

    def handle_read(self):
        data = self.recv(8192)
        if data:
            self.send(data)
            print "data: ", data
           
            
    def handle_write(self, data):
        self.send(data)

class EchoServer(asyncore.dispatcher):
    
    def __init__(self, host, port):       
        asyncore.dispatcher.__init__(self)
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
        self.set_reuse_addr()
        self.bind((host, port))
        self.listen(5)
        

    def handle_accept(self):
        pair = self.accept()
        if pair is not None:
            sock, addr = pair
            print 'Incoming connection from %s' % repr(addr)
            handler = EchoHandler(sock)
            
            handler.handle_write("2,jimi,0x00,0,10,0,0,")
            handler.handle_write("1,me,0x00,0,0,0,0,")
            xpos = 0
            for i in range(10):
                handler.handle_write("3,jimi,0x04,"+str(xpos)+",0,0,0,")
                xpos +=.1
                

server = EchoServer('localhost', 8124)
asyncore.loop()