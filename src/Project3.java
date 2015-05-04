import java.io.Serializable;
import java.net.Socket;

public class Project3 implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static int CurrentNodeId;
	static int server_port=0;
	static String topology="";
	static Socket socket1;
	public static void main(String[] args) throws Exception {
		
		// get details from file
		topology=args[1];
		CurrentNodeId = Integer.parseInt(args[0]);
		FileProp fp =new FileProp();
		fp.readNodeDetails(CurrentNodeId, topology);
		fp.readFileInformation();
		server_port=Integer.parseInt(FileProp.map.get(CurrentNodeId).split(":")[1]);
		Runnable serverTask = new Runnable() { 
			 public void run() { 
				try {
					new Server(server_port);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				} 
			}; 
			Thread serverThread = new Thread(serverTask); 
			serverThread.start();
			
			Runnable processTask = new Runnable() { 
				 public void run() { 
					new ProcessQueueMessage(); 
					} 
				}; 
				Thread processTaskThread = new Thread(processTask); 
				processTaskThread.start();
				
				
        
        
		
	}// end of main
	
	
	
} //end of class
