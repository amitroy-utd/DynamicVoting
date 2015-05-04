import java.io.File;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;


public class Protocol {
	static Socket socket = null;
	static int current_reqid=0;
	static int locktype=9;
	static String filename="";
	static int waitingtime=0;
	public void sendLockRequest(final int lock_type, final String file_name, int reqID,int waiting_time)
	{
		/*
		//DEBUG
		System.out.println("In Protocol lock type:"+lock_type+": filename is:"+file_name);
		

		for (Map.Entry<String, FileAttributes> entry : FileProp.list_files.entrySet())
			{
				String key = entry.getKey();
				//String value = entry.getValue();
				System.out.println("key is: "+key);
			}
		//DEBUG
		*/


		while(true){
		FileAttributes fb=FileProp.list_files.get(file_name);
		
		if(fb.locktype!=1)
		{		
			final int msgType=0;
			current_reqid=reqID;	
			locktype=lock_type;
			filename=file_name;
			waitingtime=waiting_time;
			
			fb.currentReqID=current_reqid;
			fb.locktype=locktype;
			if(locktype==0){
				FileProp.shared_read.put(filename, fb);
			}
			else
			{
				FileProp.exclusive_write.put(filename,fb);
			}
			for (Map.Entry<Integer, String> entry : FileProp.map.entrySet())
			{
				final int NodeID = entry.getKey();
				final String value = entry.getValue();
				final String []nodeNetInfo=value.split(":");
				Thread t = new Thread(new Runnable() {
						public void run()
						{
							try {
					        								
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
			startTimer();
			checkQuorumMembers();
			return;
			}
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
		
			 FileAttributes fas_obj_listfile=FileProp.list_files.get(filename);			 
			 fas_obj_listfile.currentReqID=0;
			 
			 FileAttributes fas_obj=new FileAttributes(current_reqid, filename, FileProp.NodeID, fas_obj_listfile.verNum, fas_obj_listfile.RU, fas_obj_listfile.M, fas_obj_listfile.P, fas_obj_listfile.Q, locktype, 0, fas_obj_listfile.requestNodeList);
			 System.out.println("buffer response size is+"+ProcessQueueMessage.bufferResponse.size());

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
				 if(locktype==1){
					 fas_obj_listfile.locktype=9;
					 FileProp.exclusive_write.remove(filename);
				 }
				 else
				 {
					 if(fas_obj_listfile.requestNodeList.isEmpty())
					 {
						 fas_obj_listfile.locktype=9;
						 FileProp.shared_read.remove(filename);
					 }
				 }
					Thread t = new Thread(new Runnable() {
						public void run()
						{
							try {
					        								
								 HandleRequest.handle_other();	
				            	
							} catch (Exception e) {
								System.out.println("Something falied: " + e.getMessage());
								e.printStackTrace();
							}
						
						}
					});
					t.start();
				    handleAbort(current_reqid,locktype, filename, waitingtime);				 
			 }
			 else if(quorun_result==1 || quorun_result==2)
			 {
				 // check for stale copy and send file
				 byte [] contents=null;
				 int node_to_send=fas_obj.Q.get(0).nodeid;
				 String tempfilename="./"+FileProp.NodeID+"/"+filename+"_"+node_to_send+"_temp";
				 File f=new File(tempfilename);
				 int staleFlag=0; // it's updated;
				 if(fas_obj.M>fas_obj_listfile.verNum)
				 {		
					 staleFlag=1;
					 sendFile(node_to_send,filename);
					 startTimer();			 
					 
				 }
				 try {
					
					if(locktype==1)
					{
						if(staleFlag==1)
						{							
							if(f.exists())
							 {
								 File currentfile=new File("./"+FileProp.NodeID+"/"+filename);
								 //currentfile.delete();
								 f.renameTo(currentfile);
							 }
							else
							{
								System.out.println("File not found");
							}
						}					
						contents=do_file_operation(filename, locktype);					
						send_release_message(fas_obj.P,contents);
						 fas_obj_listfile.verNum+=1;
						 fas_obj_listfile.RU=fas_obj.P.size();
						 fas_obj_listfile.locktype=9;
						 FileProp.exclusive_write.remove(filename);
					 }
					 else
					 {
						 contents=do_file_operation(tempfilename, locktype);					
						 send_release_message(fas_obj.P,contents);
						 if(fas_obj_listfile.requestNodeList.isEmpty())
						 {
							 fas_obj_listfile.locktype=9;
							 FileProp.shared_read.remove(filename);
						 }
					 }
					Thread t = new Thread(new Runnable() {
						public void run()
						{
							try {
					        								
								 HandleRequest.handle_other();	
				            	
							} catch (Exception e) {
								System.out.println("Something falied: " + e.getMessage());
								e.printStackTrace();
							}
						
						}
					});
					t.start();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	
	public void handleAbort(int reqID, int locktype, String filename, int waiting_time)
	{
		//This function is called if the required Quorum is not got.
		// This function retries the same message after waiting for min_wait time
		// and tries doing so until the max wait time is reached
		
		//sleep for the minimum wait time
		if (waiting_time >= FileProp.max_wait)
		{
			//stop the retry and exit from the function
		}
		else
		{
			try
			{
				
				//retry the same message after wait time (exponential back off)
				Thread.sleep(waiting_time);
		
				sendLockRequest(locktype, filename,reqID,2*waiting_time);
		
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public byte [] do_file_operation(String filename, int locktype) throws Exception
	{		
		//read the file or write into the file
		
		FileReadingWriting.FileOperation(Project3.CurrentNodeId, filename, locktype,"log.txt");
		Path file_path = Paths.get("./"+FileProp.NodeID+"/", filename);
		byte[] local_content=null;
        try
        {
             local_content =  Files.readAllBytes(file_path);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//send release message 
		//send_release_message();
		//file_action_leave();
		return local_content;
	}
	
	public void send_release_message(ArrayList<FileAttributes> P,final byte [] content){
		for(FileAttributes p_obj:P){
			String value=FileProp.map.get(p_obj.nodeid);
			final FileAttributes fab=FileProp.list_files.get(filename);
			final String []nodeNetInfo=value.split(":");
			Thread t = new Thread(new Runnable() {
					public void run()
					{
						try {
							
				        	final MessageStruct ms=new MessageStruct(current_reqid,3,FileProp.NodeID,locktype,filename,fab,content);
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
	
	public static void sendFile(int node_to_send, String filename){
		String value=FileProp.map.get(node_to_send);
		final String []nodeNetInfo=value.split(":");
		try {
			
        	final MessageStruct ms=new MessageStruct(current_reqid,4,FileProp.NodeID,filename);
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

}
