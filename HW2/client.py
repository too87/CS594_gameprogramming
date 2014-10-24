from direct.distributed.PyDatagramIterator import PyDatagramIterator
from direct.distributed.PyDatagram import PyDatagram
from panda3d.core import ConnectionWriter
from panda3d.core import NetDatagram
from panda3d.core import QueuedConnectionListener
from panda3d.core import QueuedConnectionManager
from panda3d.core import QueuedConnectionReader
import socket
import imp




#import your modules here
class client:

    def __init__(self):

    	self.username = ""
    	self.password = ""
        self.connection = None
        

    def startConnection(self):
        """Create a connection with the remote host.

        If a connection can be created, create a task with a sort value of -39
        to read packets from the socket.

        """
        
        try:

            if self.connection == None:
                #self.connection = self.cManager.openTCPClientConnection('localhost',9090,1000)
                self.connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.connection.connect(("localhost", 9090))

                if self.connection:
                    self.cReader.addConnection(self.connection)

                    taskMgr.add(self.updateRoutine, 'updateRoutine-Connection', -39)
                    taskMgr.doMethodLater(5, self.checkConnection, 'checkConnection')

                    return True



        except:
            pass
           
        

    	self.loginHandler()
        
        #send packet
        self.connection.sendall(self.username+"\n")
        self.connection.sendall(self.password+"\n")
        #receive packet
        self.fromServer = self.connection.recv(1024)
        print "from server"
        print(repr(self.fromServer))
        if self.fromServer == "yes\r\n":
            print "correct username and password"
            ralph =  __import__("Tut-Roaming-Ralph")
          
            w = ralph.World()
            w.addRalph(w.environ.find("**/start_point").getPos())
            run()
            
        else:
        	print "wrong username and password"
        return False

    def loginHandler(self):
    	self.hasAccount = raw_input("Do you have an Acount? (y/n):")
    	if(self.hasAccount == 'y'):
    		self.username = raw_input("Enter username: ")
    		self.password = raw_input("Enter password: ")
    	else:
    		print "create your new account"
    		self.username = raw_input("Enter username: ")
    		self.password = raw_input("Enter password: ")

    

    def closeConnection(self):
        
        if self.connection != None:
            taskMgr.remove('updateRoutine-Main')
            taskMgr.remove('updateRoutine-Connection')
            taskMgr.remove('checkConnection')

            self.cManager.closeConnection(self.connection)
            self.connection = None

    def sendRequest(self, requestCode, args = {}):
       
        if self.connection != None:
            request = ServerRequestTable.get(requestCode)

            if request != None:
                request.set(self.cWriter, self.connection)
                request.send(args)

    def handleResponse(self, responseCode, data):
        #Prepare a response packet to be processed.

        #If the following response code exists, create an instance of this
        #specific response using its data to be executed.

       
        response = ServerResponseTable.get(responseCode)

        if response != None:
            response.set(main)
            response.execute(data)

    def checkConnection(self, task):

        if not self.cReader.isConnectionOk(self.connection):
            self.closeConnection()
            self.showDisconnected(0)

            return task.done

        return task.again

    def showDisconnected(self, status):

        if status == 0:
            result = DatabaseHelper.dbSelectRowByID('msg', 'msg_id', 43)
            main.createMessageBox(0, result['msg_text'], main.switchEnvironment, ['Login'])
        elif status == 1:
            result = DatabaseHelper.dbSelectRowByID('msg', 'msg_id', 44)
            main.createMessageBox(0, result['msg_text'], main.switchEnvironment, ['Login'])

    def updateRoutine(self, task):
        #A once-per-frame task used to read packets from the socket.
        
       
        
        while self.cReader.dataAvailable():
            testy = 1
        
        '''
            # Create a datagram to store all necessary data.
            datagram = NetDatagram()
            
            # Retrieve the contents of the datagram.
            if self.cReader.getData(datagram):
                # Prepare the datagram to be iterated.
                data = PyDatagramIterator(datagram)
                # Retrieve a "short" that contains the response code.
                responseCode = data.getUint16()
                # Pass into another method to execute the response.
                if responseCode != 0:
                    self.handleResponse(responseCode, data)
        '''

        return task.cont
     

c = client()
c.startConnection()