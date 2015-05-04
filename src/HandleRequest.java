import java.io.IOException;


public class HandleRequest {
	
	public static synchronized void  handle_other(){
			
		while(!ProcessQueueMessage.bufferRequest.isEmpty())
		{
			MessageStruct ms=ProcessQueueMessage.bufferRequest.get(0);
			
			FileAttributes fs= FileProp.list_files.get(ms.filename);
			if(fs.locktype!=1)
			{
				if(ms.locktype==0)
				{
					try {
						ProcessQueueMessage.lockAndSendResponseRead(ms);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ProcessQueueMessage.bufferRequest.remove(0);
				}
				else if(ms.locktype==1 && fs.locktype!=0)
				{
					try {
						ProcessQueueMessage.lockAndSendResponseWrite(ms);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ProcessQueueMessage.bufferRequest.remove(0);
				}
				
			}
			else
			{
				break;
			}
		}
			
		
	}
}
