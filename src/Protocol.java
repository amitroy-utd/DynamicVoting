import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;


public class Protocol {
	static Socket socket = null;
	
	public void sendLockRequest(final int locktype, FileAttributes fa)
	{
		final int msgType=1;
		final String filename=fa.filename;
	    final int verNum=fa.verNum;	
	    
		FileProp.reqID=FileProp.reqID+1;
		
		for (Map.Entry<Integer, String> entry : FileProp.map.entrySet())
		{
			final int NodeID = entry.getKey();
			final String value = entry.getValue();
			final String []nodeNetInfo=value.split(":");
			Thread t = new Thread(new Runnable() {
					public void run()
					{
						try {
				        	//System.out.println("starting client");
		/* need to change */ FileAttributes fb=new FileAttributes(9,filename,NodeID,0,0,0,new ArrayList<FileAttributes>(),new ArrayList<FileAttributes>(),9,0,new HashSet<String>());
							final MessageStruct ms=new MessageStruct(FileProp.reqID,msgType,NodeID,locktype,filename,fb,verNum);
			            	//added
			            	ObjectOutputStream out = null;
	            			socket = new Socket(nodeNetInfo[0], Integer.parseInt(nodeNetInfo[1]));
	             
	            			out = new ObjectOutputStream(socket.getOutputStream());
	            			out.writeObject(ms);
	            			
	            			
	            			out.flush();
	            			out.close();
			            	
						} catch (Exception e) {
							System.out.println("Something falied: " + e.getMessage());
							e.printStackTrace();
						}
					
					}
				});
			t.start(); 
		}
	}
	
	
	public void startTimer()
	{
		try {
			Thread.sleep(FileProp.timer);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void checkQuorumMembers(int locktype, FileAttributes fa){
			 ArrayList<MessageStruct> responsearray=new ArrayList<MessageStruct>();	
			 FileAttributes fas_obj=FileProp.list_files.get(fa.filename);
			 for (MessageStruct ms : responsearray) {
				 // check lock ms.locktype==locktype
				 //check FileProp.reqID=ms.reqID
				 //check filename=ms.filename
				 fas_obj.P.add(ms.faobj);			

				 
		        }
			 fas_obj.M=getMaxVersionNumber(fas_obj);
			 fas_obj.Q=getQset(fas_obj);
			 fas_obj.RU=fas_obj.Q.get(0).RU;
			
	}
	
	public static int getMaxVersionNumber(FileAttributes fas){
		int max_ver=0;
		for(FileAttributes f:fas.P)
		{
			if(max_ver<f.verNum)
			{
				max_ver=f.verNum;
			}			
		}
		return max_ver;
		
	}
	
	public static ArrayList<FileAttributes> getQset(FileAttributes fas){
		ArrayList<FileAttributes> al=new ArrayList<FileAttributes>();
		for(FileAttributes f:fas.P)
		{
			if(f.M==fas.M)
			{
				al.add(f);
			}			
		}
		return al;
	}

}
