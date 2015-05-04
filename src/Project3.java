import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

public class Project3 implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static int CurrentNodeId;
	static int server_port=0;
	static String topology="";
	static Socket socket1;
	public static TreeMap<Integer, String> treemap = new TreeMap<Integer, String>();
	public static TreeMap<Integer, String> origTreemap = new TreeMap<Integer, String>();
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
				
				
			check_all_servers();
			Application ap=new Application();
			ap.sendRequest();
			System.out.println("Completed!!!");
        
		
	}// end of main
	
	public static void copyList()
	
	
	{
		for (Map.Entry<Integer, String> entry : FileProp.map.entrySet())
		{
			treemap.put(entry.getKey(), entry.getValue());
			origTreemap.put(entry.getKey(), entry.getValue());
		}
	}
	
	public static void check_all_servers() throws IOException
	{
		copyList();
		
		while(true)
		{
			
			if (treemap.isEmpty())
			{
				//call the logic because all the servers are up and running 
				System.out.println("all servers up continuing. for..."+ CurrentNodeId);
				//break out of the loop
				break;
			}
			
			for (Map.Entry<Integer, String> entry : origTreemap.entrySet())
		    {
				boolean remove_entry=false;
			
				if (treemap.containsKey(entry.getKey()))
				{
					
					//establish a socket
					String []nodeNetInfo=entry.getValue().split(":");
					try
					{
						//System.out.println("sending socket to "+ nodeNetInfo[0]+ "  "+ nodeNetInfo[1]);
						socket1=new Socket(nodeNetInfo[0],Integer.parseInt(nodeNetInfo[1]));
						MessageStruct ms = new MessageStruct(9,CurrentNodeId);
						ObjectOutputStream out = null;
						out = new ObjectOutputStream(socket1.getOutputStream());
						out.writeObject(ms);
		           		out.flush();
		           		out.close();
						if (socket1.isConnected())
						{
							remove_entry = true;
							
							//socket1.close();
						}
					}
					catch (Exception e)
					{
						//ignore exception
						//System.out.println("exception is "+e.getMessage());
					
						
					}
				}
				if (remove_entry)
				{
					System.out.println(entry.getKey() + "is connected and going to remove from treemap");
					treemap.remove(entry.getKey());
				}
		    }
			
		}
	}
} //end of class
