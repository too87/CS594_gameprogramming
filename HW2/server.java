import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;


public class server {
	public static void main(String args[]) throws IOException {
        ServerSocket listenSocket;
        String fromClient;
        String toClient = "welcome";
       // DataReader re = new DataReader();
        try {
            // Start to listen on the given port for incoming connections
            listenSocket = new ServerSocket(9090);
            System.out.println("Server has started on port: " + listenSocket.getLocalPort());
            System.out.println("Waiting for clients...");
            // Loop indefinitely to establish multiple connections
            while (true) {
                try {
                    // A client socket will represent a connection between the client and this server
                    Socket clientSocket = listenSocket.accept();
                    System.out.println("A Connection Established!");


                    ///
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
		            
		            fromClient = in.readLine();
		            
		            System.out.println("received: " + fromClient);
		            
		            int t = 1;
		            out.println(t);
		            out.println(toClient);
			        
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
    }

