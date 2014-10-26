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
       
        System.out.println("The Game server is running.");
        try
        {
            
            servSocket = new ServerSocket(9090);
            
        }
        catch (IOException e)
        {
            System.out.println("\nUnable to set up port!");
            System.exit(1);
        }

        do
        {
            //Wait for client...
            Socket client = servSocket.accept();

            System.out.println("\nNew client accepted.+"+client_num+"\n");

            client_num++;
        
            //Create a thread to handle communication with
            //this client and pass the constructor for this
            //thread a reference to the relevant socket...
            ClientHandler handler = new ClientHandler(client);

            handler.start();//As usual, this method calls run.
            clist.add(handler);
            System.out.println(clist.size());
        }while (true);
    }
    

    public static void broadcastMsg(String str){
        for(int i=0; i<clist.size(); ++i){
            ClientHandler g = clist.get(i);
            g.out.println(str);
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
        
        int id= 0;             // this is the client ID which is the same as the objects avatar ID 
        int state = 0;        // initial state should be IDLE
        float xpos = -107.5F;
        float ypos = 26.6F;
        float zpos = -0.49F;
        float hdg = 0.0F;

        public ClientHandler(Socket socket)
        {
            //Set up reference to associated socket...
            client = socket;
           
            System.out.println("New connection with client "  + " at " + socket);
            try
            {
                in = new BufferedReader(
                        new InputStreamReader(
                                client.getInputStream()));
                out = new PrintWriter(
                            client.getOutputStream(),true);
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
                String received;
                do
                {
                    //Accept message from client on
                    //the socket's input stream...
                    //broadcastMsg("client "+client_num +" entered");
                    String msg = sendState(3);
                    broadcastMsg(msg);
                    received = in.readLine();

                    //Echo message back to client on
                    //the socket's output stream...
                    

                //Repeat above until 'QUIT' sent by client...
                }while (!received.equals("QUIT"));
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
                        client.close();
                        for(Iterator<ClientHandler> i = clist.iterator(); i.hasNext();){
                            ClientHandler val = (ClientHandler)i.next();
                            if(val.client == client){
                                i.remove();
                            }
                        }
                        

                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
    }
     public String sendState(int code){
        String msg = Integer.toString(code)+","+
                    Integer.toString(this.id)+","+
                    Integer.toString(state)+","+
                    Float.toString(xpos)+","+
                    Float.toString(ypos)+","+
                    Float.toString(zpos);
        return msg;
   

    }
    
 }   
}



