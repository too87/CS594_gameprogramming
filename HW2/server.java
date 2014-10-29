import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;


public class server {
    
    static ArrayList<ClientHandler> clist = new ArrayList<ClientHandler>();
    private static ServerSocket servSocket;
    static int client_num;
    public static void main(String args[]) throws IOException {
       
      try
        {
            System.out.println("The Game server is running.");
            servSocket = new ServerSocket(9090);
        }
        catch (IOException e)
        {
            System.out.println("\nUnable to set up port!");
            System.exit(1);
        }

       while(true)
        {
            //Wait for client...
            Socket client = servSocket.accept();
            client_num++;
            System.out.println("\nNew client accepted.+"+client_num+"\n");

        
            //Create a thread to handle communication with
            //this client and pass the constructor for this
            //thread a reference to the relevant socket...
            
            ClientHandler handler = new ClientHandler(client);
            
            handler.start();
            clist.add(handler);
            
            System.out.println(clist.size());

        }
        
    }
    
    public static boolean checkUser(String username, String password){
        
        boolean flag = false;
        System.out.println(username);
        System.out.println(password);
        //check wheater the username and password match
        //assume we has this on DB usename = admin, password = 1234
        if(username.equals("admin") && password.equals("1234"))
            flag = true;

        return flag;

    }



    private static class ClientHandler extends Thread
    {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        
        int id= 0;             
        String state = "0x00";        // initial state should be IDLE
        float xpos = 0F;
        float ypos = 10F;
        float zpos = 0F;
        float hdg = 0F;

        public ClientHandler(Socket socket)
        {
            //Set up reference to associated socket...
            client = socket;
            id = client_num;
            System.out.println("New connection with client "  + " at " + socket);
            try
            {
                //client.setTcpNoDelay(true);
                in = new BufferedReader(
                        new InputStreamReader(
                                client.getInputStream()));
                out = new PrintWriter(
                            client.getOutputStream(),true);
                System.out.println(client.getPort());

            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            
            
        }

        public void run()
        {
            
            try
            {

                this.broadcastInitialState(clist);
                //while(true){
                
                for(int i=0; i<clist.size(); i++){
                    ClientHandler g = clist.get(i);
                    System.out.println("RUN(): id: "+g.id+"...."+this.id);

                   if(g.id != this.id){
                    try
                        {
                            sendState(g,2);
                            
                            /*String msg = stateMsg(2,this.id,this.state, this.xpos, this.ypos, this.zpos, this.hdg);
                            PrintWriter out = new PrintWriter(g.client.getOutputStream(),true);
                            out.println(msg);
                            System.out.println("this: "+client.getPort()+":::g.client: "+g.client.getPort());*/
                        }catch(IOException e)
                        {
                            e.printStackTrace();
                        }
                       
                        }
                    }
                    String received = null;
                    
                while ((received = in.readLine())!=null){
                    received = in.readLine();
                    System.out.println(received);
                    
                    processClientData(received);

                }
            }

            catch(IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    //when user press 'x' on screen update the list
                    if (client!=null)
                    {
                        System.out.println(
                                    "Closing down connection...");
                        
                        broadcastMsg(clist, deleteObjectMessage(this.id));

                        for(Iterator<ClientHandler> i = clist.iterator(); i.hasNext();){
                            ClientHandler val = (ClientHandler)i.next();
                            if(val.client == client){
                                i.remove();
                            }
                        }
                        client.close();

                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
      
        }
         public String stateMsg(int code, int id, String state, float xpos, float ypos, float zpos, float hdg){
            //s
            String msg = "s,"+ 
                        Integer.toString(code)+","+
                        Integer.toString(id)+","+
                        state+","+
                        Float.toString(xpos)+","+
                        Float.toString(ypos)+","+
                        Float.toString(zpos)+","+
                        Float.toString(hdg);
            return msg;
       

        }

       

        public void broadcastMsg(ArrayList<ClientHandler> clist,String str){
            for(int i=0; i<clist.size(); i++){
                ClientHandler g = clist.get(i);
                if(g.id != id)
                    g.out.println(str);
            }
        }

        public void broadcastState(ArrayList<ClientHandler>  clist){
            String msg = stateMsg(3,this.id, this.state, this.xpos, this.ypos, this.zpos, this.hdg);
            this.broadcastMsg(clist, msg);
        }

        public void broadcastInitialState(ArrayList<ClientHandler>  clist) throws IOException{
            int code =0;
            for(int i=0; i<clist.size(); i++){
                ClientHandler g = clist.get(i);
                
                if(g.id == this.id)
                    code=1;
                else
                    code=2;
                System.out.println("id: "+g.id+" this id "+this.id+" code: "+code+" port: "+g.client.getPort());
                sendState(g,code);

            }
        }

        public void sendState(ClientHandler s, int code) throws IOException {
            String msg = stateMsg(code,this.id,this.state, this.xpos, this.ypos, this.zpos, this.hdg);
            System.out.println("sendState socket id: "+s.id+" this id: "+this.id);
            PrintWriter out = new PrintWriter(s.client.getOutputStream(),true);
            out.println(msg);
        }

        public void processRpcOp(int code, int id, String state, float xpos, float ypos, float zpos, float hdg){
            switch(code){
                case 3:
                    this.id =id;
                    this.state = state;
                    this.xpos = xpos;
                    this.ypos = ypos;
                    this.zpos = zpos;
                    this.hdg = hdg;
                    this.broadcastState(clist);
                    break;
                case 5:
                    break;
                default:
                    break;
            }
        }

        public void processClientData(String data){
            String[] parts = data.split(",");
            //s 
            int code = Integer.parseInt(parts[1]);
            int id = Integer.parseInt(parts[2]);
            String state = parts[3];
            float xpos = Float.parseFloat(parts[4]);
            float ypos = Float.parseFloat(parts[5]);
            float zpos = Float.parseFloat(parts[6]);
            float hdg = Float.parseFloat(parts[7]);
            //System.out.println("processClientData"+data);
            processRpcOp(code,id,state,xpos,ypos,zpos,hdg);
            //return parts;
        }
        
        public void cleanup(){

        }

        public String deleteObjectMessage(int id){
            //s
            String msg ="s,4"+","+id;
            return msg;
        }
    }   
}



