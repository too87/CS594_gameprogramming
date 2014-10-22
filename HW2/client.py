from direct.distributed.PyDatagramIterator import PyDatagramIterator
from direct.distributed.PyDatagram import PyDatagram
from panda3d.core import ConnectionWriter
from panda3d.core import NetDatagram
from panda3d.core import QueuedConnectionListener
from panda3d.core import QueuedConnectionManager
from panda3d.core import QueuedConnectionReader

#import your modules here

class client:

    def __init__(self):

    	
        self.cManager = QueuedConnectionManager()
        self.cListener = QueuedConnectionListener(self.cManager, 0)
        self.cReader = QueuedConnectionReader(self.cManager, 0)
        self.cWriter = ConnectionWriter(self.cManager, 0)

        self.connection = None
        

    def startConnection(self):
        """Create a connection with the remote host.

        If a connection can be created, create a task with a sort value of -39
        to read packets from the socket.

        """
        
        try:

            if self.connection == None:
                self.connection = self.cManager.openTCPClientConnection('localhost',
                                                                        9090,
                                                                        1000)


                if self.connection:
                    self.cReader.addConnection(self.connection)

                    taskMgr.add(self.updateRoutine, 'updateRoutine-Connection', -39)
                    taskMgr.doMethodLater(5, self.checkConnection, 'checkConnection')

                    return True



        except:
            pass

    	self.loginHandler()
        
        #send packet
        

        myPyDatagram = PyDatagram()
        myPyDatagram.addUint8(1)
        myPyDatagram.addString(self.username)
        myPyDatagram.addString('_')
        myPyDatagram.addString(self.password)
        self.cWriter.send(myPyDatagram,self.connection)
        
        #receive packet
        datagram = NetDatagram()
        if self.cReader.getData(datagram):
        	myProcessDataFunction(datagram)
        return False

    def loginHandler(self):
    	self.username = raw_input("Enter username: ")
    	self.password = raw_input("Enter password: ")

    def myProcessDataFunction(netDatagram):
    	myIterator = PyDatagramIterator(netDatagram)
    	msgID = myIterator.getUnit8()
    	if msgID == 1:
    		msgToPrint = myIterator.getString()
    		print msgToPrint 

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

        return task.cont
     

c = client()
c.startConnection()
