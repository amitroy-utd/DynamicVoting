import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class ProcessQueueMessage extends Thread

{
	
	public static CopyOnWriteArrayList<MessageStruct> bufferResponse=new CopyOnWriteArrayList<MessageStruct>();	
	public static ArrayList<MessageStruct> bufferRequest=new ArrayList<MessageStruct>();
	
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
	        						MessageStruct ms=new MessageStruct(processObject.reqID,2,Project3.CurrentNodeId,processObject.locktype,processObject.filename);
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
	        				 //if the abort message is received for a request present in request queue,it means it was not processed still
	        				 
	        				 	if (bufferRequest.contains(processObject))
	        				 	{
	        				 		bufferRequest.remove(processObject);
	        				 	}
	        				 	//if the abort message is not in the request then it means it was processed, hence has to be removed from the file attributes
	        				 	
	        				 	else
	        				 	{
	        					
	        				 		FileAttributes current_node_file_object1 = FileProp.list_files.get(processObject.filename);
	        				 		if (current_node_file_object1.locktype==0)
	        				 		{
	        				 			current_node_file_object1.requestNodeList.remove(processObject.reqID+"-"+processObject.nodeid);
	        				 		
	        				 			if (current_node_file_object1.requestNodeList.isEmpty())
	        				 			{
	        				 				current_node_file_object1.locktype=9;
	        				 			
	        				 				//lock is free hence release from shared mode list or exclusive mode list
	        				 				if(FileProp.shared_read.containsKey(processObject.filename))
	        				 				{
	        				 					FileProp.shared_read.remove(processObject.filename);
	        				 				}
	        				 				
	        				 				//call request handle other
	        				 				
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
	        				 				
	        				 				
	        				 			}
	        				 		}
	        				 		else if(current_node_file_object1.locktype==1)
	        				 		{
	        				 			current_node_file_object1.locktype=9;
	        				 			if(FileProp.exclusive_write.containsKey(processObject.filename))
	        				 			{
	        				 				FileProp.exclusive_write.remove(processObject.filename);
	        				 			}
	        				 			
	        				 			// call request handle other because my lock is free
	        				 			
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
	        						
	        				 		}
	        				 		
	        				 		
	        				 		//FileProp.list_files.put(processObject.filename, current_node_file_object1);
	        				 	}
	        			 
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
	        						 
	        						 // call request handle other 
	        						 
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
	        						 
	        						 
	        					 }
	        					 //FileProp.list_files.put(processObject.filename, current_node_file_object1);
	        				 }
	        				 else if(processObject.locktype==1)
	        				 {
	        					 //
	    	        			 
	    	        			 //when a release message is received for a write lock, updation of the file attributes like version number ,no of copies updated and updation of the file needs to be done
	        					 FileAttributes current_node_file_object1 = FileProp.list_files.get(processObject.filename);
	        					 //current_node_file_object1.requestNodeList.remove(processObject.reqID+"-"+processObject.nodeid);
	        					 current_node_file_object1.verNum = processObject.faobj.verNum;
	        					 //number of copies updated
	        					 current_node_file_object1.RU = processObject.faobj.RU;
	        					 
	        					 //if (current_node_file_object1.requestNodeList.isEmpty())
	        					 //{
	        					 current_node_file_object1.locktype=9;
	        						
	        					if(FileProp.exclusive_write.containsKey(processObject.filename))
	        					 {
	        						 FileProp.exclusive_write.remove(processObject.filename);
	        					 }
	        						 
	        					// }
	        					 //FileProp.list_files.put(processObject.filename, current_node_file_object1);
	        					 
	        					 // replace the file with file received
	        					 
	        /*change path*/		 Path file_path = Paths.get("./"+FileProp.NodeID+"/", processObject.filename);
	        					 try
	        					 {
	        						 Files.write(file_path, processObject.contents);
	        					 }
	        					 catch (Exception e)
	        					 {
	        						 e.printStackTrace();
	        					 }
	        					 
	        					 //call request handle other
	        					 
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
	        				 }
	        			 
	                	
	             		 }
	        		 }
	        		 else if (processObject.msgType == 4) // message received is send the file
	        		 {
	                	//fetch the file and send the contents  
	        			 if(FileProp.list_files.containsKey(processObject.filename))
	        			 {
	        				 Path file_path = Paths.get("./"+FileProp.NodeID+"/", processObject.filename);
	    					 try
	    					 {
	    						 byte[] local_content =  Files.readAllBytes(file_path);
	    						 String []nodeNetInfo=FileProp.map.get(processObject.nodeid).split(":");
	    							
	    							
	    						//MessageStruct( int reqID, int msgType, int nodeid, String Filename, byte[] contents)
	    							
	    						MessageStruct ms=new MessageStruct(processObject.reqID,5,Project3.CurrentNodeId,processObject.filename,local_content);
	    					    try
	    					    {
	    					    	new Client().startClient(nodeNetInfo[0],Integer.parseInt(nodeNetInfo[1]),ms);
	    					    }
	    					    catch (Exception e)
	    					    {
	    					    	e.printStackTrace();
	    					    }
	    						 
	    					 }
	    					 catch (Exception e)
	    					 {
	    						 e.printStackTrace();
	    					 }
	        			 }
	        			 
	                	
	        		 }
	        		 else if (processObject.msgType == 5) // Received the file
	        		 {
	                	
	        			 if(FileProp.list_files.containsKey(processObject.filename))
	        			 {
/*change path*/				 Path file_path = Paths.get("./"+FileProp.NodeID+"/", processObject.filename+"_"+processObject.nodeid+"_"+"temp");
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
	
	
	public static void lockAndSendResponseRead(MessageStruct processObject) throws NumberFormatException, IOException
	{
		FileAttributes current_node_file_object = FileProp.list_files.get(processObject.filename);
		
		// update the object with request id of node and lock
		//current_node_file_object.locktype = 0;// it is already locked in read mode
		current_node_file_object.requestNodeList.add(processObject.reqID+"-"+processObject.nodeid);
		
		//update the list file list with updated attributes
		//FileProp.list_files.put(processObject.filename, current_node_file_object);
		FileProp.shared_read.put(processObject.filename, current_node_file_object);
		//get the host id and port number
		String []nodeNetInfo=FileProp.map.get(processObject.nodeid).split(":");
		
						
		//MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename, FileAttributes faobj, int verNum)
		
		MessageStruct ms=new MessageStruct(processObject.reqID,1,Project3.CurrentNodeId,0,processObject.filename,current_node_file_object,current_node_file_object.verNum);
    	try
    	{
    		new Client().startClient(nodeNetInfo[0],Integer.parseInt(nodeNetInfo[1]),ms);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
	}
	
	
	
	public static void lockAndSendResponseWrite(MessageStruct processObject) throws NumberFormatException, IOException
	{
		FileAttributes current_node_file_object = FileProp.list_files.get(processObject.filename);
		
		// update the object with request id of node and lock
		//current_node_file_object.locktype = 0;
		//current_node_file_object.requestNodeList.add(processObject.reqID+"-"+processObject.nodeid);
		
		//update the list file list with updated attributes
		//FileProp.list_files.put(processObject.filename, current_node_file_object);
		FileProp.exclusive_write.put(processObject.filename, current_node_file_object);
		//get the host id and port number
		String []nodeNetInfo=FileProp.map.get(processObject.nodeid).split(":");
		
						
		//MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename, FileAttributes faobj, int verNum)
		
		MessageStruct ms=new MessageStruct(processObject.reqID,1,Project3.CurrentNodeId,1,processObject.filename,current_node_file_object,current_node_file_object.verNum);
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
