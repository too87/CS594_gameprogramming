import sys
import struct
from asyncore import dispatcher
import socket
import state as st

from panda3d.core import Vec3

# this inherits from asyncore.dispatcher for our networking needs
class GameClient(dispatcher):

    def __init__(self, world):
        dispatcher.__init__(self)
        self.world = world
        world.client = self
        self.counter = 0
        self.xpos = 0
        
        self.id = None
        self.msg_buffer = ""    # buffer for incoming raw data

        # The dictionary of rpc operations
        # The opcode is the key to a tuple containing the function to be executed
        # and the byte length of the raw message for the rpc operation
        self.rpc_ops = { 
                            1 : [self.op_createPlayer, 21],
                            2 : [self.op_createActor, 21],
                            3 : [self.op_updateObjectPosition, 21],
                            4 : [self.op_deleteObject, 4],
                            5 : [self.op_ping, 8]
                            }

    # -------------------------------------------------------------------------
    # asyncore network code overrides
    def connect(self, host, port):
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)      # create a TCP socket
        dispatcher.connect(self, (host, port))                      # and connect
    
    def handle_connect(self):
        print 'connected'

    def handle_close(self):
        print 'disconnecting'
        sys.exit(1)

    # Implement me: add buffer handling code here to make our sending more reliable (buffering)!
    def handle_write(self):
        pass
        
    def handle_read(self):
        ''' we can pump the incoming data directly into processNetworkData because
            that function does its own buffering '''
        self.processNetworkData(self.recv(8192))    # receive max. 8192 bytes at once


    # -------------------------------------------------------------------------
    # Process incoming raw network data    
    # MS: Rewriting this to take a string instead of a packet
    def processNetworkData(self, rawdata):
        data = rawdata.replace("\n", "")
        data = data.rsplit(",")
        print "first_data: ", data
        
        while (len(data) >=7):                         
            curData = data[0:7]   
            print "cur_data: " , curData                       
            temp = self.rpc_ops[int(curData[0])]
            func = temp[0]
            func(curData)
            data = data[7:]
        
           
        
    # we send one of these to the server whenever our state changes due to user input
    # the server will then relay these messages to all other clients
    def sendClientPositionUpdate(self, objid, state, pos, hdg):
        # opcode 3: object position update
        # this contains: opcode, objid, state and 4 floats with the object's position and heading
        # movememnt state has bits for 'moving fwd', 'moving backwards', 'rotating right' , 'rotating left'

        msg = ','.join(map(str, [3, objid, state, pos[0], pos[1], pos[2], hdg])) 
        self.send(msg+"\n")

    # -------------------------------------------------------------------------
    # handlers for messages coming from the server
    
    # MS: small changes in methods below to handle a string in our protocol's format
    def op_createPlayer(self, opbuf):      
        (opcode, objid, state, xpos, ypos, zpos, hdg) = tuple(opbuf)
        print 'opcode:', opcode, ' objid:',objid, ' xpos:', xpos, ' ypos:', ypos, 'zpos:', zpos
        self.id = objid     #store the player actor object id also as client id
        player = self.world.createActor(self.id, Vec3(float(xpos), float(ypos), float(zpos)), self)
        self.world.createPlayer(player)

    def op_createActor(self, opbuf):
        (opcode, objid, state, xpos, ypos, zpos, hdg) = tuple(opbuf)
        print 'opcode:', opcode, ' objid:',objid, ' xpos:', xpos, ' ypos:', ypos, 'zpos:', zpos
        self.id = objid     
        player = self.world.createActor(self.id, Vec3(float(xpos), float(ypos), float(zpos)), self)

    def op_updateObjectPosition(self, opbuf):
        (opcode, objid, state, xpos, ypos, zpos, hdg) = tuple(opbuf)
        pos = Vec3(float(xpos), float(ypos), float(zpos))
        object = self.world.getObject(objid)
        if object is not None:
            if state == "0x00":
                state = st.LEFT
            if state == "0x02":
                state = st.RIGHT
            if state == "0x04":
                state = st.FORWARD
            if state == "0x08":
                state = st.BACKWARD
        if object is not None:
            object.motion_controller.saveNetState([state, pos, float(hdg)])
        
    def op_deleteObject(self, opbuf):
        print 'processing deleteObject message, objid=', objid
        self.world.deleteObject(objid)

    def op_ping(self, opbuf):
        (opcode, timestamp, lag) = struct.unpack("<HIH", opbuf)
        print 'processing ping message, incoming timestamp:', timestamp, ' server lag:', lag
        self.world.inst8.setText('Current connection lag: ' + str(lag) + ' ms')
        # simply send it back
        self.send(msg)