import java.io.Serializable;
class MessageStruct implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int reqID;
	int msgType; //0 - req 1-response 3-abort
    int nodeid ;
    int locktype; // 0 - read 1 - write
    String filename;
    int verNum;
    FileAttributes faobj;
    
    

    public  MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename, FileAttributes faobj, int verNum)
    {
       this.reqID=reqID;
       this.msgType=msgType;
       this.nodeid=nodeid;
       this.locktype=locktype;
       this.faobj=faobj;
       this.filename=Filename;
       this.verNum=verNum;
    }

   
}
