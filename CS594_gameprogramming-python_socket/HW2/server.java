import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;


public class server {
	String username;
	String password;
	String url;
	Connection c= null;
	public server()
	{
	try{
	 url = "jdbc:mysql://localhost/ralphs";
             username = "root";
             password = "";
            
             c = DriverManager.getConnection( url, username, password );
            
            
			}
			catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void main(String args[]) throws IOException {
        ServerSocket listenSocket;
        String fromClient_username;
        String fromClient_password;
        String toClient;
        Integer count = 0;
       
        try {
            // Start to listen on the given port for incoming connections
            listenSocket = new ServerSocket(9090);
            System.out.println("Server has started on port: " + listenSocket.getLocalPort());
            System.out.println("Waiting for clients...");
            // Loop indefinitely to establish multiple connections
            while (true) {
                try {
                	count++;
                    // A client socket will represent a connection between the client and this server
                    Socket clientSocket = listenSocket.accept();
                    System.out.println("A Connection Established!::"+count);


                    ///
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
		            
		            fromClient_username = in.readLine();
		            fromClient_password = in.readLine();
		            
		            //check username pass 
		            boolean check = checkUser(fromClient_username,fromClient_password);
		            if(check != false)
		            	out.println("yes");

		            else
		            	out.println("no");

		            //send info back to client

		            //out.println(toClient);
			        
		            ////
		            

                    // Create a thread to represent a client that holds the client socket
                    //GameClient client = new GameClient(clientSocket, this);
                    // Run the thread
                    //client.start();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        }
    public static boolean checkUser(String username, String password){
    	
    	System.out.println("username is:"+username);
    	System.out.println("password is:"+password);
    	boolean flag = false;
    	//check wheater the username and password match
    	//assume we has this on DB usename = admin, password = 1234
    	if(username.equals("admin") && password.equals("1234"))
    		flag = true;

    	return flag;

    }

    }

