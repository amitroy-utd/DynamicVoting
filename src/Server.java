
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
 
 
public class Server implements Serializable {
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static BlockingQueue<MessageStruct> unbounded = new LinkedBlockingQueue<MessageStruct>();
	
	ServerSocket myServerSocket;
    boolean ServerOn = true;
 
 
    public Server(final int portnum) throws ClassNotFoundException 
    { 
        try
        { 
        	System.out.println("In server");
            myServerSocket = new ServerSocket(portnum); 
        } 
        catch(IOException ioe) 
        { 
            System.out.println("Could not create server socket on port . Quitting."); 
            System.exit(-1); 
        } 
 
 
 
        // Successfully created Server Socket. Now wait for connections. 
        while(ServerOn) 
        {                        
            try
            { 
                // Accept incoming connections. 
                Socket clientSocket = myServerSocket.accept(); 
 
                // accept() will block until a client connects to the server. 
                // If execution reaches this point, then it means that a client 
                // socket has been accepted. 
 
                // For each client, the received object is added onto a linkedblocking queue
                // ProcessQueMessage thread reads from this queue and processes each request in 
                // a thread
                
                
                
                                               
                ObjectInputStream in = null;
                in = new ObjectInputStream(clientSocket.getInputStream());
    			Object obj = in.readObject();
    			
    				
    			MessageStruct msgrcvd = null;
                    
    			msgrcvd = (MessageStruct) obj;
    			unbounded.add(msgrcvd);
                in.close();
                
 
               // ClientServiceThread cliThread = new ClientServiceThread(clientSocket);
                //cliThread.start(); 
 
            } 
            catch(IOException ioe) 
            { 
                System.out.println("Exception encountered on accept. Ignoring. Stack Trace :"); 
                ioe.printStackTrace(); 
            } 
 
        }
 
        try
        { 
            myServerSocket.close(); 
            System.out.println("Server Stopped"); 
        } 
        catch(Exception ioe) 
        { 
            System.out.println("Problem stopping server socket"); 
            System.exit(-1); 
        } 
 
 
 
    } 
 
  
 /*
    class ClientServiceThread extends Thread 
    { 
        Socket myClientSocket;
        boolean m_bRunThread = true; 
 
        public ClientServiceThread() 
        { 
            super(); 
        } 
 
        ClientServiceThread(Socket s) 
        { 
            myClientSocket = s; 
 
        } 
 
        public void run() 
        {            
            
        	ObjectInputStream in = null;
            
            // Print out details of this connection 
            System.out.println("Accepted Client "); 
 
            try
            {                                
            	in = new ObjectInputStream(myClientSocket.getInputStream());
				Object obj = in.readObject();
				System.out.println("object is"+obj);
				
				MessageStruct msgrcvd = null;
                
                msgrcvd = (MessageStruct) obj;
                in.close();
                
                if (msgrcvd.msgType == 0) // the received message is a request from a node for keys 
                {
                	
                	
                	
                }
                else if (msgrcvd.msgType == 1) // message received is a response with keys
                {
                	
                	
                	
                }
               
                else
                {
                	System.out.println("connection check");
                	
                }
                
                
                
                
            } 
            catch(Exception e) 
            { 
                e.printStackTrace(); 
                e.getMessage();
            } 
           
        } 
       	
    } */
}
