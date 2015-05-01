import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;


public class Protocol {
	static Socket socket = null;
	static int current_reqid=0;
	static int locktype=9;
	static String filename="";
	public void sendLockRequest(final int lock_type, final String file_name, int reqID)
	{
		final int msgType=0;
		current_reqid=reqID;	
		locktype=lock_type;
		filename=file_name;
		for (Map.Entry<Integer, String> entry : FileProp.map.entrySet())
		{
			final int NodeID = entry.getKey();
			final String value = entry.getValue();
			final String []nodeNetInfo=value.split(":");
			Thread t = new Thread(new Runnable() {
					public void run()
					{
						try {
				        	FileAttributes fb=FileProp.list_files.get(filename);
							fb.currentReqID=current_reqid;
							final MessageStruct ms=new MessageStruct(current_reqid,msgType,FileProp.NodeID,locktype,filename);
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
	
	public void checkQuorumMembers(){
			 //ArrayList<MessageStruct> responsearray=new ArrayList<MessageStruct>();	
			 //responsearray=ProcessQueueMessage.bufferResponse;
		
			 FileAttributes fas_obj=FileProp.list_files.get(filename);			 
			 fas_obj.currentReqID=0;
			 for (MessageStruct ms : ProcessQueueMessage.bufferResponse) 
			 {
				 // check lock ms.locktype==locktype
				 //check FileProp.reqID=ms.reqID
				 //check filename=ms.filename
				 if(ms.locktype==locktype && filename.equals(ms.filename) && ms.reqID==current_reqid)
				 {
					 fas_obj.P.add(ms.faobj);
				 }				 					 
		     }
			 ProcessQueueMessage.bufferResponse.clear();
			 fas_obj.M=getMaxVersionNumber(fas_obj);
			 fas_obj.Q=getQset(fas_obj);
			 fas_obj.RU=fas_obj.Q.get(0).RU;
			 int quorun_result=calculateQuorum(fas_obj.RU,fas_obj.Q.size());
			 if(quorun_result==0){
				 sendAbort(fas_obj.P);
				 
			 }
			 		 
			
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
	
	public static int calculateQuorum(int ru,int q_size){
		int Q_val=0;
		Q_val=ru/2;
		if(Q_val>q_size)
		{
			return 1;
		}
		else if(Q_val==q_size){
			return 2;
		}
		else
		{
			return 0;
		}
	}
	
	public static void sendAbort(ArrayList<FileAttributes> P){
		for(FileAttributes p_obj:P){
			String value=FileProp.map.get(p_obj.nodeid);
			final String []nodeNetInfo=value.split(":");
			Thread t = new Thread(new Runnable() {
					public void run()
					{
						try {
				        	final MessageStruct ms=new MessageStruct(current_reqid,2,FileProp.NodeID,locktype,filename);
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

}
