import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;


public class ProcessQueueMessage extends Thread

{
	int NodeID;
	public static CopyOnWriteArrayList<MessageStruct> bufferResponse=new CopyOnWriteArrayList<MessageStruct>();	
	public static CopyOnWriteArrayList<MessageStruct> bufferRequest=new CopyOnWriteArrayList<MessageStruct>();

	public void run() {
	      while (true)
	      {
	    	  if (!(Server.unbounded.isEmpty()))
	    	  {
	    		  try {
	    			  execute(Server.unbounded.take());
	    		  	  }
	    		  catch (InterruptedException e) {
	    	   }
	    	  }
	      }  
	   }
	
	
	private void execute(final MessageStruct processObject) {
	      new Thread(new Runnable() {
	         public void run() {
	            //request.execute();
	        	 try
	        	 {
	        		 if (processObject.msgType == 0) // the received message is a read request or write request 
	        		 {
	                	if(processObject.locktype == 0)// READ REQUEST 
	                	{
	                		if (FileProp.shared_read.containsKey(processObject.filename))
	                		{
	                			// file is in shared mode, can send the lock back to the requesting node
	                			
	                			if (FileProp.list_files.containsKey(processObject.filename))
	                			{
	                				lockAndSendResponseRead(processObject);
	                			}
	                			
	                			
	                		}
	                		else if (FileProp.exclusive_write.containsKey(processObject.filename))
	                		{
	                			//file is in write mode so buffer the request
	                			
	                			if (FileProp.list_files.containsKey(processObject.filename))
	                			{
	                				bufferRequest.add(processObject);
	                				
	                			}
	                			
	                		}
	                		else
	                		{
	                			//the file requested is free (that means lock type is 9),so lock and send the lock to requested node
	                			
	                			//Get the file requested for read from the list_files map 
	                			if (FileProp.list_files.containsKey(processObject.filename))
	                			{
	                				lockAndSendResponseRead(processObject);
	                			}
	                			
	                		}
	                	}
	                	else // its a write lock
	                	{
	                		if ((FileProp.shared_read.containsKey(processObject.filename)) || (FileProp.exclusive_write.containsKey(processObject.filename)))
	                		{
	                			// buffer the request as the file requested is either in read or write mode
	                			if (FileProp.list_files.containsKey(processObject.filename))
	                			{
	                				bufferRequest.add(processObject);
	                			}
	                		
	                		}
	                		else 
	                		{
	                			//file is free so lock the local copy in write mode and send the lock
	                			
	                			//Get the file requested for read from the list_files map 
	                			if (FileProp.list_files.containsKey(processObject.filename))
	                			{
	                				lockAndSendResponseWrite(processObject);
	                			}
	                			
	                		
	                		}
	                	}
	                	
	                	
	        		 }
	        		 else if (processObject.msgType == 1) // message received is a response to the read or write request
	        		 {
	                	
	        			if((processObject.locktype == 0)|| (processObject.locktype == 1))// response received for read lock
		                {
	        				//add it to the response queue
	        				if (FileProp.list_files.containsKey(processObject.filename))
                			{
	        					
	        					FileAttributes current_node_file_object1 = FileProp.list_files.get(processObject.filename);
	        					// if the reply is received for the current request buffer
	        					if (current_node_file_object1.currentReqID==processObject.reqID)
	        					{
	        						bufferResponse.add(processObject);
	        					}
	        					//if the reply received is for a old request then send abort back
	        					else
	        					{
	        						//Sendabort(processObject);
	        						
	        						//check with Amit about sending the abort
	        						String []nodeNetInfo=FileProp.map.get(processObject.nodeid).split(":");
	        						
	        						
	        						//                   MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename, FileAttributes faobj, int verNum)
	        						MessageStruct ms=new MessageStruct(processObject.reqID,2,NodeID,processObject.locktype,processObject.filename);
	        				    	try
	        				    	{
	        				    		new Client().startClient(nodeNetInfo[0],Integer.parseInt(nodeNetInfo[1]),ms);
	        				    	}
	        				    	catch (Exception e)
	        				    	{
	        				    		e.printStackTrace();
	        				    	}
	        						//ignore the request assuming 
	        						
	        					}
                			}
	        				
		                }
	        			/*else if (processObject.locktype == 1)// response received for write lock
		                {
	        				//add it to the response queue
		                }
	                	*/
	        		 }
	        		 else if (processObject.msgType == 2) // message received is abort
	        		 {

	        			 //when receiving a abort for the request , remove the lock secured for it
	        			 
	        			 if (FileProp.list_files.containsKey(processObject.filename))
             			{
	        					
	        					FileAttributes current_node_file_object1 = FileProp.list_files.get(processObject.filename);
	        					current_node_file_object1.requestNodeList.remove(processObject.reqID+"-"+processObject.nodeid);
	        					
	        					if (current_node_file_object1.requestNodeList.isEmpty())
	        					{
	        						current_node_file_object1.locktype=9;
	        						
	        						//lock is free hence release from shared mode list or exclusive mode list
	        						if(FileProp.shared_read.containsKey(processObject.filename))
	        						{
	        							FileProp.shared_read.remove(processObject.filename);
	        						}
	        						else if(FileProp.exclusive_write.containsKey(processObject.filename))
	        						{
	        							FileProp.exclusive_write.remove(processObject.filename);
	        						}
	        						
	        					}
	        					FileProp.list_files.put(processObject.filename, current_node_file_object1);
             			}
	        			 
	                	
	                	
	        		 }
	        		 else if (processObject.msgType == 3) // message received is release
	        		 {
	        			 
	        			 
	        			
	        			 if (FileProp.list_files.containsKey(processObject.filename))
	             		 {
	        				 //when a release message is received, for read lock, just the lock type needs to be released
	        				 if (processObject.locktype == 0)
	        				 {
	        					 FileAttributes current_node_file_object1 = FileProp.list_files.get(processObject.filename);
	        					 current_node_file_object1.requestNodeList.remove(processObject.reqID+"-"+processObject.nodeid);
	        					 if (current_node_file_object1.requestNodeList.isEmpty())
	        					 {
	        						 current_node_file_object1.locktype=9;
	        						
	        						 //lock is free hence release from shared mode list or exclusive mode list
	        						 if(FileProp.shared_read.containsKey(processObject.filename))
	        						 {
	        							 FileProp.shared_read.remove(processObject.filename);
	        						 }
	        						 
	        						 
	        					 }
	        					 FileProp.list_files.put(processObject.filename, current_node_file_object1);
	        				 }
	        				 else if(processObject.locktype==1)
	        				 {
	        					 //
	    	        			 
	    	        			 //when a release message is received for a write lock, updation of the file attributes like version number ,no of copies updated and updation of the file needs to be done
	        					 FileAttributes current_node_file_object1 = FileProp.list_files.get(processObject.filename);
	        					 current_node_file_object1.requestNodeList.remove(processObject.reqID+"-"+processObject.nodeid);
	        					 current_node_file_object1.verNum = processObject.verNum;
	        					 //number of copies updated
	        					 current_node_file_object1.RU = processObject.RU;
	        					 
	        					 if (current_node_file_object1.requestNodeList.isEmpty())
	        					 {
	        						 current_node_file_object1.locktype=9;
	        						
	        						if(FileProp.exclusive_write.containsKey(processObject.filename))
	        						 {
	        							 FileProp.exclusive_write.remove(processObject.filename);
	        						 }
	        						 
	        					 }
	        					 FileProp.list_files.put(processObject.filename, current_node_file_object1);
	        					 
	        					 // replace the file with file received
	        					 
	        					 Path file_path = Paths.get("D:\\Studies\\Perl", processObject.filename+"_"+FileProp.NodeID+"_"+"temp");
	        					 try
	        					 {
	        						 Files.write(file_path, processObject.contents);
	        					 }
	        					 catch (Exception e)
	        					 {
	        						 e.printStackTrace();
	        					 }
	        				 }
	        			 
	                	
	             		 }
	        		 }
	        		 else if (processObject.msgType == 4) // message received is send the file
	        		 {
	                	//fetch the file and send the contents 
	                	
	                	
	        		 }
	        		 else if (processObject.msgType == 5) // Received the file
	        		 {
	                	
	                	
	                	
	        		 }
	        		 else
	        		 {
	        			 System.out.println("connection check");
	                	
	        		 }	
	        	 }
	        	 catch (Exception e)
	        	 {
	        		 e.printStackTrace();
	        	 }
	         }
	      }).start();
	   }
	
	
	public void lockAndSendResponseRead(MessageStruct processObject) throws NumberFormatException, IOException
	{
		FileAttributes current_node_file_object = FileProp.list_files.get(processObject.filename);
		
		// update the object with request id of node and lock
		//current_node_file_object.locktype = 0;// it is already locked in read mode
		current_node_file_object.requestNodeList.add(processObject.reqID+"-"+processObject.nodeid);
		
		//update the list file list with updated attributes
		FileProp.list_files.put(processObject.filename, current_node_file_object);
		FileProp.shared_read.put(processObject.filename, current_node_file_object);
		//get the host id and port number
		String []nodeNetInfo=FileProp.map.get(processObject.nodeid).split(":");
		
						
		//MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename, FileAttributes faobj, int verNum)
		
		MessageStruct ms=new MessageStruct(processObject.reqID,1,NodeID,0,processObject.filename,current_node_file_object,current_node_file_object.verNum);
    	try
    	{
    		new Client().startClient(nodeNetInfo[0],Integer.parseInt(nodeNetInfo[1]),ms);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
	}
	
	
	
	public void lockAndSendResponseWrite(MessageStruct processObject) throws NumberFormatException, IOException
	{
		FileAttributes current_node_file_object = FileProp.list_files.get(processObject.filename);
		
		// update the object with request id of node and lock
		//current_node_file_object.locktype = 0;
		current_node_file_object.requestNodeList.add(processObject.reqID+"-"+processObject.nodeid);
		
		//update the list file list with updated attributes
		FileProp.list_files.put(processObject.filename, current_node_file_object);
		FileProp.exclusive_write.put(processObject.filename, current_node_file_object);
		//get the host id and port number
		String []nodeNetInfo=FileProp.map.get(processObject.nodeid).split(":");
		
						
		//MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename, FileAttributes faobj, int verNum)
		
		MessageStruct ms=new MessageStruct(processObject.reqID,1,NodeID,1,processObject.filename,current_node_file_object,current_node_file_object.verNum);
    	try
    	{
    		new Client().startClient(nodeNetInfo[0],Integer.parseInt(nodeNetInfo[1]),ms);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
	}
}
