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
		 
        
        
		
	}// end of main
	
	
	
} //end of class
